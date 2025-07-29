package com.revenga.rits.smartscheduler.lib.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.exception.ExceptionUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class AES {

	private static SecretKeySpec secretKey;
	private static final String SECRET = "Revenga.19";

	private AES() {

		throw new IllegalStateException(this.getClass().getSimpleName());
	}

	public static String encrypt(final String strToEncrypt) throws Exception {

		String enc = null;

		setKey(SECRET);
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);

		enc = Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));

		return enc;
	}

	public static String decrypt(final String strToDecrypt) throws Exception {

		String dec = null;

		setKey(SECRET);
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, secretKey);

		dec = new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));

		return dec;
	}

	private static void setKey(final String myKey) {

		MessageDigest sha = null;
		byte[] key;

		try {

			key = myKey.getBytes(StandardCharsets.UTF_8);
			sha = MessageDigest.getInstance("SHA-1");
			key = sha.digest(key);
			key = Arrays.copyOf(key, 16);
			secretKey = new SecretKeySpec(key, "AES");

		} catch (NoSuchAlgorithmException e) {

			log.error(e.getMessage());
			log.debug(ExceptionUtils.getStackTrace(e));
		}
	}
}
