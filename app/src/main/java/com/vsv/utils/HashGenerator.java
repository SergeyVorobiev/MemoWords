package com.vsv.utils;

import androidx.annotation.NonNull;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

public class HashGenerator {

    private static final byte[] salt = new byte[]{-23, 90, 76, 55, 45, -108, 44, 9, 15, -99, -75, 115, 4, -78, -122, 68};

    private static final byte[] salt2 = new byte[]{46, 69, -81, -8, -18, 98, 52, 57, -79, -126, 88, -1, -32, -115, 67, -12};

    private static String convertToHex(final byte[] messageDigest) {
        BigInteger bigint = new BigInteger(1, messageDigest);
        String hexText = bigint.toString(16);
        while (hexText.length() < 32) {
            hexText = "0".concat(hexText);
        }
        return hexText;
    }

    @NonNull
    public static String getSSH(@NonNull String string) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            return convertToHex(md.digest(string.getBytes(StandardCharsets.UTF_16)));
        } catch (Exception e) {
            return "null";
        }
    }

    @NonNull
    public static String getMD5(@NonNull String string) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(salt2);
            return convertToHex(md.digest(string.getBytes(StandardCharsets.UTF_16)));
        } catch (Exception e) {
            return "null";
        }
    }

    public static byte[] createSalt() throws Exception {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt;
    }
}
