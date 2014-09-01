package org.akita.ui.async;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.Toast;
import org.akita.exception.AkException;
import org.akita.util.Log;

/**
 * Created with IntelliJ IDEA.
 * Date: 12-4-9
 * Time: 上午11:20
 *
 * @author zhe.yangz
 */
public abstract class SimpleAsyncTask<T> extends AsyncTask<Integer, Integer, T> {
    private static final String TAG = "SimpleAsyncTask<T>";
    protected AkException mAkException = null;
    private Context mContext = null;

    /**
     * guarantees the method be invoked on ui thread once time when task start.
     */
    protected void onUITaskStart() {};

    @Override
    protected void onPreExecute() {
        super.onPreExecute();    //defaults

        onUITaskStart();
        try{
            onUIBefore();
        } catch (AkException akException) {
            mAkException = akException;
        }
    }

    public AsyncTask<Integer, Integer, T> fire() {
        return execute(0);
    }

    public AsyncTask<Integer, Integer, T> fireOnParallel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 0);
        } else {
            return execute(0);
        }
    }

    public AsyncTask<Integer, Integer, T> fire(Context context) {
        mContext = context;
        return fire();
    }

    public AsyncTask<Integer, Integer, T> fireOnParallel(Context context) {
        mContext = context;
        return fireOnParallel();
    }

    @Override
    protected T doInBackground(Integer... integers) {
        try {
            if (mAkException == null) {
                return onDoAsync();
            } else {
                return null;
            }
        } catch (AkException akException) {
            mAkException = akException;
            return null;
        }
    }

    protected abstract void onUIBefore() throws AkException;
    protected abstract T onDoAsync() throws AkException;
    /**
     * it may not be executed if have exception before.
     * @param t
     * @throws AkException
     */
    protected abstract void onUIAfter(T t) throws AkException;

    @Override
    protected void onPostExecute(T t) {
        super.onPostExecute(t);    //defaults

        if (mAkException != null) {
            onHandleAkException(mAkException);
        } else {
            try {
                onUIAfter(t);
            } catch (AkException akException) {
                onHandleAkException(mAkException);
            }
        }
        try {
            onUITaskEnd();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCancelled(T t) {
        try {
            onUITaskEnd();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onHandleAkException(AkException mAkException) {
        Log.w(TAG, mAkException.toString(), mAkException);

        if (mContext != null) {
            Toast.makeText(mContext, mAkException.toString(), Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * guarantees the method be invoked on ui thread once time when task quit.
     */
    protected void onUITaskEnd() {};

    /**
     * public of the method publishProgress, but must also be called in doinbackground.
     * @param values
     */
    public void publishProgressPublic(Integer... values) {
        publishProgress(values);
    }

}
