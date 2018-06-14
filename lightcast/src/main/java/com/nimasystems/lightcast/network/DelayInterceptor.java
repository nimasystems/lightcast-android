package com.nimasystems.lightcast.network;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

public class DelayInterceptor implements Interceptor {

    private long mDelayTime;

    public DelayInterceptor(Context context, long delayTime) {
        setDelayTime(delayTime);
    }

    public void setDelayTime(long delayTime) {
        mDelayTime = delayTime;
    }

    public long getDelayTime() {
        return mDelayTime;
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {

        if (mDelayTime > 0) {
            try {
                Thread.sleep(mDelayTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return chain.proceed(chain.request());
    }
}
