package com.vulnbank.app.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;
import android.util.Log;

/**
 * CryptoHelper - Cryptographic utilities
 *
 * INTENTIONAL VULNERABILITIES:
 * 1. Uses DES (weak algorithm, 56-bit key)
 * 2. Hardcoded encryption key
 * 3. Hardcoded IV (Initialization Vector)
 * 4. Uses ECB mode (no IV, identical blocks produce identical ciphertext)
 * 5. Falls back to Base64 as "encryption"
 */
public class CryptoHelper {

    private static final String TAG = "CryptoHelper";

    // VULNERABILITY: Hardcoded DES key (too short, predictable)
    private static final String DES_KEY = "vuln1234";  // DES requires exactly 8 bytes

    // VULNERABILITY: Hardcoded AES key
    private static final String AES_KEY = "1234567890abcdef";  // 16 bytes for AES-128

    // VULNERABILITY: Hardcoded IV
    private static final byte[] HARDCODED_IV = {0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07};

    // VULNERABILITY: Uses DES/ECB - weak cipher with no IV
    public static String encryptDES(String plaintext) {
        try {
            SecretKeySpec key = new SecretKeySpec(DES_KEY.getBytes("UTF-8"), "DES");
            // VULNERABILITY: ECB mode - same plaintext produces same ciphertext
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(plaintext.getBytes("UTF-8"));
            return Base64.encodeToString(encrypted, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "DES encryption failed: " + e.getMessage());
            // VULNERABILITY: Falls back to plain Base64 "encoding" as "encryption"
            return Base64.encodeToString(plaintext.getBytes(), Base64.DEFAULT);
        }
    }

    public static String decryptDES(String ciphertext) {
        try {
            SecretKeySpec key = new SecretKeySpec(DES_KEY.getBytes("UTF-8"), "DES");
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decrypted = cipher.doFinal(Base64.decode(ciphertext, Base64.DEFAULT));
            return new String(decrypted, "UTF-8");
        } catch (Exception e) {
            Log.e(TAG, "DES decryption failed: " + e.getMessage());
            return new String(Base64.decode(ciphertext, Base64.DEFAULT));
        }
    }

    // VULNERABILITY: AES with hardcoded key and IV
    public static String encryptAES(String plaintext) {
        try {
            SecretKeySpec key = new SecretKeySpec(AES_KEY.getBytes("UTF-8"), "AES");
            // VULNERABILITY: Hardcoded IV
            IvParameterSpec iv = new IvParameterSpec(new byte[16]); // All zeros IV
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            byte[] encrypted = cipher.doFinal(plaintext.getBytes("UTF-8"));
            return Base64.encodeToString(encrypted, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "AES encryption failed: " + e.getMessage());
            return Base64.encodeToString(plaintext.getBytes(), Base64.DEFAULT);
        }
    }

    public static String decryptAES(String ciphertext) {
        try {
            SecretKeySpec key = new SecretKeySpec(AES_KEY.getBytes("UTF-8"), "AES");
            IvParameterSpec iv = new IvParameterSpec(new byte[16]);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] decrypted = cipher.doFinal(Base64.decode(ciphertext, Base64.DEFAULT));
            return new String(decrypted, "UTF-8");
        } catch (Exception e) {
            Log.e(TAG, "AES decryption failed: " + e.getMessage());
            return new String(Base64.decode(ciphertext, Base64.DEFAULT));
        }
    }

    // VULNERABILITY: "Encrypt" is just Base64 encoding
    public static String fakeEncrypt(String data) {
        return Base64.encodeToString(data.getBytes(), Base64.DEFAULT);
    }

    public static String fakeDecrypt(String data) {
        return new String(Base64.decode(data, Base64.DEFAULT));
    }
}
