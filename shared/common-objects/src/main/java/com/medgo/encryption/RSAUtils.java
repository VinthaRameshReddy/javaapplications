package com.medgo.encryption;

import com.medgo.constant.AppConstants;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Stateless RSA helper – all methods are static.
 * Only exception‑handling logic has been changed; crypto logic is untouched.
 */
public final class RSAUtils {

	private static final Logger logger = LoggerFactory.getLogger(RSAUtils.class);

	private RSAUtils() { /* utility */ }

	/* ------------------------------------------------------------------ */
	/* Key helpers                                                        */
	/* ------------------------------------------------------------------ */

	public static PublicKey getPublicKey(String base64PublicKey) {
		try {
			byte[] decoded = Base64.getDecoder().decode(base64PublicKey);
			X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
			return KeyFactory.getInstance(AppConstants.RSA).generatePublic(spec);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			logger.error("RSAUtils getPublicKey :: {}", ExceptionUtils.getStackTrace(e));
			throw new IllegalStateException("Unable to reconstruct RSA public key", e);
		}
	}

	public static PrivateKey getPrivateKey(String base64PrivateKey) {
		try {
			byte[] decoded = Base64.getDecoder().decode(base64PrivateKey);
			PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
			return KeyFactory.getInstance(AppConstants.RSA).generatePrivate(spec);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			logger.error("RSAUtils getPrivateKey :: {}", ExceptionUtils.getStackTrace(e));
			throw new IllegalStateException("Unable to reconstruct RSA private key", e);
		}
	}

	/* ------------------------------------------------------------------ */
	/* Encryption / Decryption                                            */
	/* ------------------------------------------------------------------ */

	public static byte[] encrypt(String data,
								 String publicKey) throws BadPaddingException,
			IllegalBlockSizeException,
			InvalidKeyException,
			NoSuchPaddingException,
			NoSuchAlgorithmException {
		try {
			Cipher cipher = Cipher.getInstance(AppConstants.RSA_ECB_PKCS1Padding);
			cipher.init(Cipher.ENCRYPT_MODE, getPublicKey(publicKey));
			return cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
		} catch (BadPaddingException | IllegalBlockSizeException |
				 InvalidKeyException | NoSuchPaddingException |
				 NoSuchAlgorithmException e) {
			logger.error("RSAUtils encrypt :: {}", ExceptionUtils.getStackTrace(e));
			throw e;                   // preserve original checked‑exception contract
		}
	}

	public static String decrypt(byte[] data,
								 PrivateKey privateKey) throws NoSuchPaddingException,
			NoSuchAlgorithmException,
			InvalidKeyException,
			BadPaddingException,
			IllegalBlockSizeException {
		try {
			Cipher cipher = Cipher.getInstance(AppConstants.RSA_ECB_PKCS1Padding);
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			return new String(cipher.doFinal(data), StandardCharsets.UTF_8);
		} catch (BadPaddingException | IllegalBlockSizeException |
				 InvalidKeyException | NoSuchPaddingException |
				 NoSuchAlgorithmException e) {
			logger.error("RSAUtils decrypt :: {}", ExceptionUtils.getStackTrace(e));
			throw e;
		}
	}

	public static String decrypt(String data,
								 String base64PrivateKey) throws IllegalBlockSizeException,
			InvalidKeyException,
			BadPaddingException,
			NoSuchAlgorithmException,
			NoSuchPaddingException {
		return decrypt(Base64.getDecoder().decode(data), getPrivateKey(base64PrivateKey));
	}

	public static String rsaDecryption(String data,
									   String privateKey) throws InvalidKeyException,
			BadPaddingException,
			IllegalBlockSizeException,
			NoSuchPaddingException,
			NoSuchAlgorithmException {
		return decrypt(data, privateKey);   // delegate, keeps original behaviour
	}
}
