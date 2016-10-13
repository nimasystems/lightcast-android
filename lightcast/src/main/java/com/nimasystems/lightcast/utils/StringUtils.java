package com.nimasystems.lightcast.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StringUtils {
    private static final String[] cyrLetters = {"а", "б", "в", "г", "д", "е", "ё", "ж", "з", "и", "й", "к", "л", "м", "н", "о", "п", "р", "с", "т", "у", "ф", "х", "ц", "ч", "ш", "щ", "ъ", "ы", "ь", "э", "ю", "я", "А", "Б", "В", "Г", "Д", "Е", "Ж", "З", "И", "Й", "К", "Л", "М", "Н", "О", "П", "Р", "С", "Т", "У", "Ф", "Х", "Ц", "Ч", "Ш", "Щ", "Ъ", "Ы", "Ь", "Э", "Ю", "Я"};
    private static final String[] latLetters = {"a", "b", "v", "g", "d", "e", "io", "zh", "z", "i", "y", "k", "l", "m", "n", "o", "p", "r", "s", "t", "u", "f", "h", "ts", "ch", "sh", "sht", "a", "i", "y", "e", "yu", "ya", "A", "B", "V", "G", "D", "E", "Zh", "Z", "I", "Y", "K", "L", "M", "N", "O", "P", "R", "S", "T", "U", "F", "H", "Ts", "Ch", "Sh", "Sht", "A", "I", "Y", "E", "Yu", "Ya"};

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

        for (int i = 0; i <= t.length - 1; i++) {
            ret.add(IntegerUtils.getSafeInteger(t[i]));
        }

        return ret;
    }

    public static ArrayList<Long> explodeLong(String separator, String str) {
        if (str == null) {
            return null;
        }

        ArrayList<Long> ret = new ArrayList<>();

        String[] t = str.split(separator);

        for (int i = 0; i <= t.length - 1; i++) {
            ret.add(IntegerUtils.getSafeLong(t[i]));
        }

        return ret;
    }

    public static boolean safeEqualsIgnoreCase(String string1, String string2) {
        return ((string1 == null ? string2 == null : string1.equalsIgnoreCase(string2)));
    }

    public static boolean safeEquals(String string1, String string2) {
        return ((string1 == null ? string2 == null : string1.equals(string2)));
    }

    public static String cyrToLatTransliterate(String cyrStr, boolean doInverse) {

        String out = cyrStr;

        if (StringUtils.isNullOrEmpty(cyrStr)) {
            return null;
        }

        if (!doInverse) {
            for (int i = 0; i <= cyrLetters.length - 1; i++) {
                out = out.replace(cyrLetters[i], latLetters[i]);
            }
        } else {
            for (int i = 0; i <= latLetters.length - 1; i++) {
                out = out.replace(latLetters[i], cyrLetters[i]);
            }
        }

        return out;
    }
}
