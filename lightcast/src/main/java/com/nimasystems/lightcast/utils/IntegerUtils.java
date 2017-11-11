package com.nimasystems.lightcast.utils;

import java.util.Random;

public class IntegerUtils {
    public static int getSafeInteger(String str) {
        int ret = 0;

        try {
            ret = Integer.parseInt(str);
        } catch (Exception e) {
            //e.printStackTrace();
        }

        return ret;
    }

    public static double getSafeDouble(String str) {
        double ret = 0;

        try {
            ret = Double.parseDouble(str);
        } catch (Exception e) {
            //e.printStackTrace();
        }

        return ret;
    }

    public static long getSafeLong(String str) {
        long ret = 0;

        try {
            ret = Long.parseLong(str);
        } catch (Exception e) {
            //e.printStackTrace();
        }

        return ret;
    }

    public static float getSafeFloat(String str) {
        float ret = 0;

        try {
            ret = Float.parseFloat(str);
        } catch (Exception e) {
            //e.printStackTrace();
        }

        return ret;
    }

    public static int getRandomNumber(int min, int max) {
        Random r = new Random();
        return r.nextInt(max - min) + min;
    }

    public static int getRandomNumber() {
        int max = Integer.MAX_VALUE;
        int min = Integer.MIN_VALUE;
        Random r = new Random();
        return r.nextInt(max - min) + min;
    }
}
