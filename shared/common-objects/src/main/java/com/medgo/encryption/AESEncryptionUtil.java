package com.medgo.encryption;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Stateless AES helper.  All methods are static, the class cannot be instantiated.
 */
public final class AESEncryptionUtil {

    private static final String ALGORITHM  = "AES";
    private static final String SECRET_KEY = "1234567890123456";               // 16‑byte key
    private static final SecretKeySpec KEY =
            new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);

    private AESEncryptionUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /** Encrypt plain text and return Base‑64 string. */
    public static String encrypt(String data) {
        if (data == null) return null;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, KEY);
            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Encryption error", e);
        }
    }

    /** Decrypt Base‑64 AES string back to plain text. */
    public static String decrypt(String encryptedData) {
        if (encryptedData == null) return null;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, KEY);
            byte[] decoded   = Base64.getDecoder().decode(encryptedData);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption error", e);
        }
    }
}
