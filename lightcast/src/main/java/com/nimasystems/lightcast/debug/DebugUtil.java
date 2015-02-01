package com.nimasystems.lightcast.debug;

import android.util.Log;

public class DebugUtil {
    public static void printCurrentStackTrace() {
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            //System.out.println(ste);
            Log.e("SYS", ste.toString());
        }
    }
}