package com.nimasystems.lightcast.utils;

import android.content.Context;

public class LocationUtils {
    public static boolean getHasLocationAccessRights(Context context) {
        return true;
        // TODO: Reenable this when 6.0 compat has been done
        /*
        return (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);*/
    }
}
