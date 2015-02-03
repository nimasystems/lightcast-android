package com.nimasystems.lightcast.ui.fonts;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

import java.util.TreeMap;

public class TypefaceCache {

    private static TypefaceCache typefaceSingleton;
    private TreeMap<String, Typeface> fontCache = new TreeMap<>();

    public static TypefaceCache getSingleton() {
        if (typefaceSingleton == null) {
            typefaceSingleton = new TypefaceCache();
        }
        return typefaceSingleton;
    }

    public Typeface getFont(Context context, String fontName) {
        Typeface tf = fontCache.get(fontName);
        if (tf == null) {
            try {
                tf = Typeface.createFromAsset(context.getAssets(), fontName);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            fontCache.put(fontName, tf);
        }
        return tf;
    }

    public void setFont(Context context, TextView tv, String fontName) {
        try {
            tv.setTypeface(getFont(context, fontName));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}