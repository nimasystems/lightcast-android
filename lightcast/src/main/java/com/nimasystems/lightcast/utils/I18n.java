package com.nimasystems.lightcast.utils;

import android.content.Context;

public class I18n {

    /**
     * Get phone default locale.
     *
     * @param context - application context
     * @return locale formatted as String Object.
     */
    public static String getLocaleCode(Context context) {
        return context.getResources().getConfiguration().locale.toString();
    }

}
