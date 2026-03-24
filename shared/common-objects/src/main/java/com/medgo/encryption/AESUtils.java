package com.medgo.encryption;

import com.medgo.constant.AppConstants;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public final class AESUtils {

	private static final Logger logger = LoggerFactory.getLogger(AESUtils.class);

	/* ── constants, no longer changeable at runtime ─────────────────────────── */
	private static final int    KEY_SIZE        = 128;                // bits
	private static final int    ITERATION_COUNT = 1000;               // kept for reference
	private static final String TRANSFORMATION  = AppConstants.AES_CBC_PKCS5Padding;
	private static final String ALGORITHM       = AppConstants.AES;

	private AESUtils() {
		throw new UnsupportedOperationException("Utility class");
	}

	/* ── public API ─────────────────────────────────────────────────────────── */

	/**
	 * Encrypt plain text using a key|iv combo string (key and IV are hex).
	 *
	 * @param keyWithIv  format: {@code <hexKey>|<hexIv>}
	 * @param plaintext  UTF‑8 plain text
	 * @return Base‑64 encoded cipher text
	 */
	public static String encrypt(String keyWithIv, String plaintext) {
		try {
			String[] parts = keyWithIv.split("\\|");
			SecretKeySpec key = new SecretKeySpec(hex(parts[0]), ALGORITHM);
			String iv         = parts[1];

			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(hex(iv)));
			byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
			return base64(encrypted);
		} catch (Exception e) {
			logger.error("AESUtils.encrypt :: {}", ExceptionUtils.getStackTrace(e));
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Decrypt cipher text using a key|iv combo string (key and IV are hex).
	 *
	 * @param keyWithIv   format: {@code <hexKey>|<hexIv>}
	 * @param ciphertext  Base‑64 encoded cipher text
	 * @return UTF‑8 plain text
	 */
	public static String decrypt(String keyWithIv, String ciphertext) {
		try {
			String[] parts = keyWithIv.split("\\|");
			SecretKeySpec key = new SecretKeySpec(hex(parts[0]), ALGORITHM);
			String iv         = parts[1];

			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(hex(iv)));
			byte[] decrypted = cipher.doFinal(base64(ciphertext));
			return new String(decrypted, StandardCharsets.UTF_8);
		} catch (Exception e) {
			logger.error("AESUtils.decrypt :: {}", ExceptionUtils.getStackTrace(e));
			throw new IllegalStateException(e);
		}
	}

	/* ── helper methods (unchanged in spirit, but static) ───────────────────── */

	public static String randomHex(int length) {
		byte[] random = new byte[length];
		new SecureRandom().nextBytes(random);
		return hex(random);
	}

	private static String base64(byte[] bytes) {
		return new String(Base64.encodeBase64(bytes));
	}

	private static byte[] base64(String str) {
		return Base64.decodeBase64(str.getBytes());
	}

	private static String hex(byte[] bytes) {
		return new String(Hex.encodeHex(bytes));
	}

	private static byte[] hex(String str) {
		try {
			return Hex.decodeHex(str.toCharArray());
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
