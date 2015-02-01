package com.nimasystems.lightcast.ui.fonts;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FontUtils {

    public static void setTypefaceToAll(Activity activity, String fontName) {
        View view = activity.findViewById(android.R.id.content).getRootView();
        setTypefaceToAll(activity.getApplicationContext(), view, fontName);
    }

    public static void setTypefaceToAll(Context context, View view,
                                        String fontName) {
        if (view instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) view;
            int count = g.getChildCount();
            for (int i = 0; i < count; i++) {
                setTypefaceToAll(context, g.getChildAt(i), fontName);
            }
        } else if (view instanceof TextView) {
            TextView tv = (TextView) view;
            setTypeface(context, tv, fontName);
        }
    }

    public static void setTypefaceToAllWithTag(Context context, View view,
                                               int tag, String fontName) {
        if (view instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) view;
            int count = g.getChildCount();
            TextView v;
            for (int i = 0; i < count; i++) {
                if (g.getChildAt(i) instanceof TextView) {
                    v = (TextView) g.getChildAt(i);

                    if ((Integer) v.getTag() == tag) {
                        FontUtils.setTypeface(context, v, fontName);
                    }
                }
            }
        } else if (view instanceof TextView) {
            TextView tv = (TextView) view;
            setTypeface(context, tv, fontName);
        }
    }

    public static void setTypeface(Context context, TextView tv, String fontName) {
        TypefaceCache.getSingleton().setFont(context, tv, fontName);
    }
}
