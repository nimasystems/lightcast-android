package com.nimasystems.lightcast.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StringUtils {

    public static boolean isNullOrEmpty(String str) {
        return (str == null || str.length() < 1);
    }

    public static String getRandomString() {
        return Long.toHexString(Double.doubleToLongBits(Math.random()));
    }

    /**
     * Method to join array elements of type string
     *
     * @param inputArray Array which contains strings
     * @param glueString String between each array element
     * @return String containing all array elements separated by glue string
     * @author Hendrik Will, imwill.com
     */
    public static String implodeArray(String[] inputArray, String glueString) {

        /** Output variable */
        String output = "";

        if (inputArray.length > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(inputArray[0]);

            for (int i = 1; i < inputArray.length; i++) {
                sb.append(glueString);
                sb.append(inputArray[i]);
            }

            output = sb.toString();
        }

        return output;
    }

    /*
     * public static String stringJoin(List<?> list, String delim) {
     *
     * StringBuilder sb = new StringBuilder();
     *
     * String loopDelim = "";
     *
     * for (String s : list) {
     *
     * sb.append(loopDelim); sb.append(s);
     *
     * loopDelim = delim; }
     *
     * return sb.toString(); }
     */
    public static String join(List<?> list, char delimiter) {
        if (list == null) {
            return null;
        }

        StringBuilder result = new StringBuilder();
        for (Iterator<?> i = list.iterator(); i.hasNext(); ) {
            result.append(i.next());
            if (i.hasNext()) {
                result.append(delimiter);
            }
        }
        return result.toString();
    }

    public static ArrayList<Integer> explodeInt(String separator, String str) {
        if (str == null) {
            return null;
        }

        ArrayList<Integer> ret = new ArrayList<>();

        String[] t = str.split(separator);

        if (t == null) {
            return null;
        }

        for (int i = 0; i <= t.length - 1; i++) {
            ret.add(IntegerUtils.getSafeInteger(t[i]));
        }

        return ret;
    }

    public static ArrayList<Long> explode(String separator, String str) {
        if (str == null) {
            return null;
        }

        ArrayList<Long> ret = new ArrayList<>();

        String[] t = str.split(separator);

        if (t == null) {
            return null;
        }

        for (int i = 0; i <= t.length - 1; i++) {
            ret.add(IntegerUtils.getSafeLong(t[i]));
        }

        return ret;
    }
}
