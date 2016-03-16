package com.nimasystems.lightcast.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.view.WindowManager;

public class ScreenUtils {

    private static Point mScrDimensions;

    public static boolean isHighDensity(Context context) {
        return context != null && (context.getResources().getDisplayMetrics().density >= 2);

    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public static Point getDisplaySize(Activity activity) {
        if (mScrDimensions != null) {
            return mScrDimensions;
        }
        WindowManager wm = activity.getWindowManager();
        Point dims = new Point();
        if (android.os.Build.VERSION.SDK_INT >= 13) {
            wm.getDefaultDisplay().getSize(dims);
        } else if (android.os.Build.VERSION.SDK_INT < 13) {
            dims.x = wm.getDefaultDisplay().getWidth();
            dims.y = wm.getDefaultDisplay().getHeight();
        }
        mScrDimensions = dims;
        return dims;
    }

    public static float dpToPx(Context context, float dp) {
        if (context == null) {
            return -1;
        }
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public static float pxToDp(Context context, float px) {
        if (context == null) {
            return -1;
        }
        return px / context.getResources().getDisplayMetrics().density;
    }
}
