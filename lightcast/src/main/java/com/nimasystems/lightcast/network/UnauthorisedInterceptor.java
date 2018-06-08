package com.nimasystems.lightcast.network;

import android.content.Context;
import android.support.annotation.NonNull;

import com.nimasystems.lightcast.utils.StringUtils;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Okio;

public class UnauthorisedInterceptor implements Interceptor {

    private UnauthorizedInterceptorListener listener;

    @SuppressWarnings("FieldCanBeLocal")
    private Context mContext;

    public UnauthorizedInterceptorListener getListener() {
        return listener;
    }

    public void setListener(UnauthorizedInterceptorListener listener) {
        this.listener = listener;
    }

    public UnauthorisedInterceptor(Context context) {
        mContext = context;
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());

        if (listener != null) {
            JSONObject obj = null;
            int responseCode = response.code();

            if (responseCode == 403) {
                ResponseBody body = response.body();

                if (body != null) {
                    MediaType mt = body.contentType();

                    if (mt != null) {
                        String mtType = mt.type();
                        String mtSubtype = mt.subtype();

                        if (StringUtils.safeEquals(mtType, "application") &&
                                StringUtils.safeEquals(mtSubtype, "json")) {

                            try {
                                String s = Okio.buffer(body.source()).readUtf8();

                                if (!StringUtils.isNullOrEmpty(s)) {
                                    obj = new JSONObject(s);
                                }
                            } catch (Exception e) {
                                //  nothing here
                                e.printStackTrace();
                            }
                        }
                    }
                }

                listener.OnUnauthorizedInterceptorAction(responseCode, obj);
            }
        }

        return response;
    }
}
