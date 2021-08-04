package com.nimasystems.lightcast.network;

import okhttp3.Call;
import okhttp3.OkHttpClient;

public class OkHttpUtils {
    public static void cancelCallWithTag(OkHttpClient client, String tag) {
        for (Call call : client.dispatcher().queuedCalls()) {
            Object tag1 = call.request().tag();
            if (tag1 != null && tag1.equals(tag))
                call.cancel();
        }
        for (Call call : client.dispatcher().runningCalls()) {
            Object tag1 = call.request().tag();
            if (tag1 != null && tag1.equals(tag))
                call.cancel();
        }
    }
}