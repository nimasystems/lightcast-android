package com.nimasystems.lightcast.utils;

import android.util.Log;

import com.nimasystems.lightcast.debug.DebugUtil;

public class DebugUtils {

    public static void ass(boolean condition, @SuppressWarnings("UnusedParameters") String message) {
        if (!condition) {
            Log.e("SYS", "ASSERT FAILED!!!");
            DebugUtil.printCurrentStackTrace();
        }

		/*
         * if (message != null) { Assert.assertTrue(message, condition); } else
		 * { Assert.assertTrue(condition); }
		 */
    }

    public static void ass(boolean condition) {
        DebugUtils.ass(condition, null);
    }

}
