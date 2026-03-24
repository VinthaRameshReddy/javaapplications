package com.medgo.encryption;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EncData {

	private static final Logger logger = LoggerFactory.getLogger(EncData.class);

	private EncData() { }

	/**
	 * Decrypt the request body with the AES key obtained by first
	 * RSA‑decrypting the token.
	 */
	public static String getReqDecryptedData(String inputString,
											 String encToken,
											 String privateKey) throws Exception {
		try {
			String aesKey = RSAUtils.rsaDecryption(encToken, privateKey);
			return AESUtils.decrypt(aesKey, inputString);
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Encrypt the response body with the supplied AES key.
	 */
	public static String getResEncData(String response,
									   String aesKey) throws Exception {
		try {
			return AESUtils.encrypt(aesKey, response);
		} catch (Exception e) {
			logger.error("EncData getResEncData :: {}", ExceptionUtils.getStackTrace(e));
			throw e;
		}
	}
}
