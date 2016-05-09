package com.groupchat.tools;

import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by yanyu on 4/7/2016.
 */
public class SHA1 {
    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static MessageDigest digest;

    static {
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            Log.w("SHA1", "No SHA-1 algorithm found", e);
        }
    }

    public static final String hash(String toHash) {
        String hash = null;
        try {
            digest.reset();
            byte[] bytes = toHash.getBytes("UTF-8");
            digest.update(bytes, 0, bytes.length);
            bytes = digest.digest();
            hash = bytesToHex(bytes);
        } catch (Exception e) {
            Log.w("SHA1", "Exception while getting SHA-1", e);
        }
        return hash;
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j*2] = hexArray[ v >>> 4 ];
            hexChars[j*2 + 1] = hexArray[ v & 0x0F ];
        }
        return new String(hexChars);
    }
}
