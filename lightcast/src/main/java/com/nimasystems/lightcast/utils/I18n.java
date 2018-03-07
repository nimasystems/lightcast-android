package com.nimasystems.lightcast.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.LocaleList;

import java.util.Locale;

public class I18n {

    /**
     * Get phone default locale.
     *
     * @param context - application context
     * @return locale formatted as String Object.
     */
    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public static Locale getLocale(Context context) {
        if (SysUtils.androidSupportsSDK(24)) {
            LocaleList locales = context.getResources().getConfiguration().getLocales();
            return (locales.size() > 0 ? locales.get(0) : null);
        } else {
            return context.getResources().getConfiguration().locale;
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public static String getLocaleCode(Context context) {
        Locale locale = getLocale(context);
        return (locale != null ? locale.toString() : null);
    }
}
