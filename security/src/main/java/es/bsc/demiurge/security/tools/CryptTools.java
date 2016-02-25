package es.bsc.demiurge.security.tools;


import es.bsc.demiurge.core.configuration.Config;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.Sha2Crypt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

/**
 * @author Mario Macias (http://github.com/mariomac)
 */
public class CryptTools {
	private static final int SALT_SIZE = 32;
	private static final String DIGEST_ALGORITHM = "SHA-256";

	public static final boolean verify(String digestA, String digestB) {
		try {
			return MessageDigest.getInstance(DIGEST_ALGORITHM).isEqual(stringToByte(digestA), stringToByte(digestB));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static final boolean verify(String plainStr, byte[] salt, String digestStr) {
		MessageDigest digest = null;
		try {
			byte[] digestA = getHashWithSalt(plainStr, salt);
			System.out.println("bytetoString(digestA) = " + bytetoString(digestA));
			return MessageDigest.getInstance(DIGEST_ALGORITHM).isEqual(digestA, stringToByte(digestStr));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return false;
		}

	}

	public static byte[] getSalt() {
		SecureRandom RND = new SecureRandom();
		byte[] salt = new byte[SALT_SIZE];
		RND.nextBytes(salt);
		return salt;
	}

	public static final byte[] crypt(String plainString) {
		byte[] salt = getSalt();
		try {
			return getHashWithSalt(plainString, salt);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}



	private static String bytetoString(byte[] input) {
		return org.apache.commons.codec.binary.Base64.encodeBase64String(input);
	}

	private static byte[] getHashWithSalt(String input, byte[] salt) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		digest.reset();
		digest.update(salt);
		byte[] hashedBytes = digest.digest(stringToByte(input));
		return hashedBytes;
	}
	private static byte[] stringToByte(String input) {
		if (Base64.isBase64(input)) {
			return Base64.decodeBase64(input);

		} else {
			return Base64.encodeBase64(input.getBytes());
		}
	}

}
