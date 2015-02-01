package com.nimasystems.lightcast.utils;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Hash {
    public static byte[] generateHmacSHA256Signature(byte[] data, String key)
            throws GeneralSecurityException {
        byte[] hmacData;
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes("UTF-8"),
                    "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);
            hmacData = mac.doFinal(data);
            return hmacData;
        } catch (UnsupportedEncodingException e) {
            throw new GeneralSecurityException(e);
        }
    }

}
