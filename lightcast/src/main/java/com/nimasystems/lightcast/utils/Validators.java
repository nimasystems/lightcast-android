package com.nimasystems.lightcast.utils;

import android.annotation.SuppressLint;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validators {

    @SuppressLint("NewApi")
    public static boolean isValidEmail(CharSequence target) {

        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static boolean isValidUsername(String username) {
        String USERNAME_PATTERN = "^[A-Za-z0-9_]{0,30}$";
        Pattern pattern = Pattern.compile(USERNAME_PATTERN);

        Matcher matcher = pattern.matcher(username);
        Boolean result = matcher.matches();

        return result;
    }

}
