package com.otblabs.jiinueboda.utility;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class EncryptionUtil {

    private static final String AES_ALGORITHM = "AES";

    public static String encrypt(String plainText,String encryptionKey) throws Exception {
        SecretKey secretKey = new SecretKeySpec(encryptionKey.getBytes(StandardCharsets.UTF_8), AES_ALGORITHM);
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decrypt(String encryptedText,String encryptionKey) throws Exception {
        SecretKey secretKey = new SecretKeySpec(encryptionKey.getBytes(StandardCharsets.UTF_8), AES_ALGORITHM);
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

//    public static void main(String[] args) {
//        try {
//            System.out.println(encrypt("pass","key"));
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

    private static String generateRandomKey(int keySize) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] key = new byte[keySize];
        secureRandom.nextBytes(key);

        StringBuilder result = new StringBuilder();
        for (byte b : key) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}

