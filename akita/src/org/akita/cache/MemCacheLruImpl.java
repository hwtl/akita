/*
 * Copyright 2012 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package org.akita.cache;

import android.util.LruCache;

import java.util.Map;


/**
 * Caution: use this impl only API Level >= 12
 * @author zhe.yangz 2012-3-30 下午03:23:19
 */
public class MemCacheLruImpl<K, V> implements MemCache<K, V> {

    private LruCache<K, V> mCache = null;

    protected MemCacheLruImpl(int maxSize){
        mCache = new LruCache<K, V>(maxSize);

    }
    
    public V get(K key){
        return mCache.get(key);
    }
    
    public V put(K key, V value) {
        return mCache.put(key, value);
    }
    
    public V remove(K key) {
        return mCache.remove(key);
    }

    @Override
    public void clear() {
        mCache.evictAll();
    }

    public Map<K, V> snapshot() {
        return mCache.snapshot();
    }
}
