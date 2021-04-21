package com.nimasystems.lightcast.utils;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Hash {
    public static byte[] generateHmacSHA256Signature(byte[] data, String key)
            throws GeneralSecurityException {
        byte[] hmacData;
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKey);
        hmacData = mac.doFinal(data);
        return hmacData;
    }

}
