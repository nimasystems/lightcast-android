package com.nimasystems.lightcast.utils;

import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class ScreenUtils {

    private static Point mScrDimensions;

    public static boolean isHighDensity(Context context) {
        return context != null && (context.getResources().getDisplayMetrics().density >= 2);

    }

    public static Point getDisplaySize(Context context) {
        if (mScrDimensions != null) {
            return mScrDimensions;
        }

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point dims = new Point();

        wm.getDefaultDisplay().getSize(dims);

        mScrDimensions = dims;

        return dims;
    }

    public static float dpToPx(Context context, float dp) {
        if (context == null) {
            return -1;
        }

        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static float pxToDp(Context context, float px) {
        if (context == null) {
            return -1;
        }

        return px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}
