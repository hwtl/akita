/*
 * Copyright 2012 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.akita.cache;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import com.alibaba.akita.util.FileUtil;
import com.alibaba.akita.util.HashUtil;
import com.alibaba.akita.util.Log;

import java.io.File;
import java.net.URLEncoder;
import java.security.spec.EncodedKeySpec;

/**
 * 在SD卡中存储文件夹的实现 
 * 要求key.length >= 4
 * @author zhe.yangz 2012-3-31 上午09:51:01
 */
public abstract class FilesCacheSDFoldersImpl<V> implements FilesCache<V> {
    protected static final String TAG = "FilesCacheSDFoldersImpl";

    private static final String CACHE_SIZE_KEY = "cacheSizeInMB";
    private static final String PREF_PREFIX = "filescachesd_";
    /**
     * 当evict时，判断多少小时前的文件夹会被删除
     */
    private static final int CACHE_EVICT_HOURS = 48;
    /**
     * 默认Cache Size MB
     */
    private static final int DEFAULT_CACHE_SIZE_MB = 150;
    /**
     * 小于该值的图片会缓存在软引用Mem中
     */
    private static final int MEM_BITMAP_BYTE = 8000000;

    protected String mCacheTag;
    private MemCache<String, V> mSoftBitmapCache;
    protected Context mContext;
    protected int mCacheSizeInMB;

    /**
     * 
     */
    protected FilesCacheSDFoldersImpl(Context context, String cacheTag){
        mContext = context;
        mCacheTag = cacheTag;
        mSoftBitmapCache = new MemCacheSoftRefImpl<String, V>();

        // in Config
        SharedPreferences sp = context.getSharedPreferences(PREF_PREFIX + cacheTag, 0);
        mCacheSizeInMB = sp.getInt(CACHE_SIZE_KEY, DEFAULT_CACHE_SIZE_MB);

        // create dir if not exist
        try {
            File dir = new File(getSepcifiedCacheDir());
            dir.mkdirs();

            for (int idx = 10; idx < 30; idx++) {
                File f = new File(dir, String.valueOf(idx)+"/");
                f.mkdir();
            }
        } catch (Exception e) {
            /* no op */
        }

    }

    protected abstract V xform(String fileAbsoPathAndName);
    protected abstract void output(String fileAbsoPath, String fileName, V v, String key);
    
    private String mapRule(String key) {
        // because the chinese char in url will break the md5.
        return HashUtil.md5(URLEncoder.encode(key));
    }

    private String getSpecifiedCacheFileName(String hashedKey) {
        return hashedKey + ".cache";
    }

    private String getSepcifiedCacheDir() {
        return Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/Android/data/" + mContext.getPackageName() + "/cache/" + mCacheTag + "/";
    }

    private String getSpecifiedCacheFilePath(String hashedKey) {
        String path = getSepcifiedCacheDir() + (Math.abs(hashedKey.hashCode()) % 20 + 10) + "/";
        /* + hashedKey.substring(2, 4) + "/";*/
        return path;
    }
    
    @Override
    public V get(String key) {
        V bm = mSoftBitmapCache.get(key);
        if (bm != null) {
            return bm;
        } else {
            bm = doLoad(mapRule(key));
            if (bm != null) {
                if (bm instanceof Bitmap && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                    Bitmap bitmap = (Bitmap) bm;
                    if (bitmap.getByteCount() < MEM_BITMAP_BYTE) {
                        mSoftBitmapCache.put(key, bm);
                    }
                } else {
                    mSoftBitmapCache.put(key, bm);
                }
            }
            return bm;
        }
    }

    private V doLoad(String hashedKey) {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {    // We can read and write the media

        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {    // We can only read the media

        } else {    // Something else is wrong. It may be one of many other states, but all we need
                    // to know is we can neither read nor write
              return null;
        }

        String pathAndName = getSpecifiedCacheFilePath(hashedKey)
                + getSpecifiedCacheFileName(hashedKey);
        File f = new File(pathAndName);
        if (f.exists()) {
            return xform(pathAndName);
        } else {
            return null;
        }
    }

    @Override
    public V put(String key, V value) {
        if (value != null) {
            V oldV = remove(key);
            doSave(mapRule(key), value, key);
            if (value instanceof Bitmap && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                Bitmap bitmap = (Bitmap) value;
                if (bitmap.getByteCount() < MEM_BITMAP_BYTE) {
                    mSoftBitmapCache.put(key, value);
                }
            } else {
                mSoftBitmapCache.put(key, value);
            }
            return oldV;
        }
        return null;
    }

    /**
     * @param hashedKey
     * @param value
     */
    private void doSave(String hashedKey, V value, String key) {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) { // We can read and write the media

        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) { // We can only read the media
            Log.w(TAG, "need WRITE_EXTERNAL_STORAGE permission, otherwise part of cache can be used.");
            return;
        } else { // Something else is wrong. It may be one of many other states, but all we need
                 // to know is we can neither read nor write
            return;
        }

        // doSave V to the sd cache filesystem
        String path = getSpecifiedCacheFilePath(hashedKey);
        output(path, getSpecifiedCacheFileName(hashedKey), value, key);
    }

    @Override
    public V remove(String key) {
        return doDelete(mapRule(key));
    }

    private V doDelete(String hashedKey) {
        V oldV = null;
        String pathAndName = getSpecifiedCacheFilePath(hashedKey)
                + getSpecifiedCacheFileName(hashedKey);
        File f = new File(pathAndName);
        if (f.exists()) {
            oldV = doLoad(hashedKey);
            f.delete();
        }
        return oldV;
    }

    /**
     * When current size > mCacheSizeInMB, then remove some data.
     * note: maybe time-consuming
     */
    @Override
    public void evict() {
        double size = FileUtil.getFileSizeMB(new File(getSepcifiedCacheDir()));
        if (size > mCacheSizeInMB) {
            // junk it
            File cacheDir = new File(getSepcifiedCacheDir());
            File[] dirs = cacheDir.listFiles();
            for (File dir : dirs) {
                if (dir.isDirectory()) {
                    long diff = System.currentTimeMillis() - dir.lastModified();
                    if (diff > CACHE_EVICT_HOURS * 60 * 60 * 1000) {
                        FileUtil.deleteFileOrDir(dir);
                    }
                }
            }
        }
    }

    public double getCacheCurrentSizeMB() {
        return FileUtil.getFileSizeMB(new File(getSepcifiedCacheDir()));
    }

    @Override
    public void clearCache() {
        try{
            File cacheDir = new File(getSepcifiedCacheDir());
            FileUtil.deleteFileOrDir(cacheDir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setCacheSize(int cacheSizeInMB) {
        mCacheSizeInMB = cacheSizeInMB;
        SharedPreferences sp = mContext.getSharedPreferences(PREF_PREFIX + mCacheTag, 0);
        if (mCacheSizeInMB != sp.getInt(CACHE_SIZE_KEY, DEFAULT_CACHE_SIZE_MB)) {
            sp.edit().putInt(CACHE_SIZE_KEY, mCacheSizeInMB).apply();
        }
    }
}
