package com.nimasystems.lightcast.encoding;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sha1 {

    public static String getHash(String str) {
        MessageDigest digest;
        byte[] input = null;

        try {
            digest = MessageDigest.getInstance("SHA-1");
            digest.reset();
            input = digest.digest(str.getBytes("UTF-8"));

        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        return convertToHex(input);
    }

    public static String getHash(byte[] data) {
        MessageDigest digest;
        byte[] input = null;

        try {
            digest = MessageDigest.getInstance("SHA-1");
            digest.reset();
            input = digest.digest(data);

        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
        return convertToHex(input);
    }

    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

}
