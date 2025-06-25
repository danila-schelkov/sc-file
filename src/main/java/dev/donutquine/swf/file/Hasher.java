package dev.donutquine.swf.file;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public final class Hasher {
    private static final MessageDigest MD5;

    static {
        try {
            MD5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private Hasher() {
    }

    public static boolean verifyHash(byte[] data, byte[] hash) {
        return Arrays.equals(createHash(data), hash);
    }

    public static byte[] createHash(byte[] data) {
        return MD5.digest(data);
    }
}
