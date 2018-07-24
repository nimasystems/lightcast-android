package com.nimasystems.lightcast.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;

import java.security.MessageDigest;

public class SocialNetworks {

    public static String getGooglePlusUrl(String packageName) {
        return "http://play.google.com/store/apps/details?id="
                + packageName;
    }

    /*public static String getFacebookKeyHash(Context context) {
        String keyhash = null;
        try {
            @SuppressLint("PackageManagerGetSignatures") PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getApplicationContext().getPackageName(),
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                keyhash = Base64.encodeToString(md.digest(), Base64.DEFAULT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return keyhash;
    }*/
}
