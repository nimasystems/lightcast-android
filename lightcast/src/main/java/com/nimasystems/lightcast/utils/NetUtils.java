package com.nimasystems.lightcast.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Uri;

import java.net.URL;

public class NetUtils {

    public static Uri getUriFromUrl(URL url) {
        Uri u = null;

        try {
            String p = url.getPath();

            // for whatever reason we end up with double slashes if this is not
            // done...
            if (p != null && p.length() > 0 && p.substring(0, 1).equals("/")) {
                p = p.substring(1, p.length());
            }

            Uri.Builder builder = new Uri.Builder().scheme(url.getProtocol())
                    .encodedAuthority(url.getAuthority()).appendEncodedPath(p);
            u = builder.build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return u;
    }

    public static boolean setMobileDataEnabled(Context context, boolean enabled) {
        boolean success = true;
        try {
            final ConnectivityManager conman = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            final Class<?> conmanClass = Class.forName(conman.getClass()
                    .getName());
            final java.lang.reflect.Field iConnectivityManagerField = conmanClass
                    .getDeclaredField("mService");
            iConnectivityManagerField.setAccessible(true);
            final Object iConnectivityManager = iConnectivityManagerField
                    .get(conman);
            final Class<?> iConnectivityManagerClass = Class
                    .forName(iConnectivityManager.getClass().getName());
            final java.lang.reflect.Method setMobileDataEnabledMethod = iConnectivityManagerClass
                    .getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
            setMobileDataEnabledMethod.setAccessible(true);

            setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        }
        return success;
    }
}
