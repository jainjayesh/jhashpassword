package de.janbusch.hashpassword.core;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This utility class contains static methods for generating passwords from
 * passphrases with MD5, SHA1, SHA256, SHA384 or SHA512.
 * 
 * @author Algorithm: Juergen Busch
 * @author Adjustments for JAVA: Jan Busch
 * 
 */
public class HashUtil {
	/**
	 * This method returns a password generated out of the given information.
	 * 
	 * @param host
	 *            Name of the host as {@link String}.
	 * @param login
	 *            Name of the login as {@link String}.
	 * @param passphrase
	 *            Passphrase for generating password as {@link String}.
	 * @param hashType
	 *            The hash type on which the password should be generated as
	 *            {@link EHashType}.
	 * @param characterSet
	 *            Character set which should be used for generating the password
	 *            as {@link String}.
	 * @param maxPwLength
	 *            The maximal length of the generated password as int.
	 * @return The generated password as {@link String}.
	 */
	public static String generatePassword(String host, String login,
			String passphrase, EHashType hashType, String characterSet,
			int maxPwLength) {

		String baseString;
		if (login.length() == 0) {
			baseString = passphrase + "@" + host;
		} else {
			baseString = login + "@" + host + "#" + passphrase;
		}
		byte[] digest = HashUtil.createHash(hashType, baseString);

		int digestLength = digest.length;
		String sPassword = "";

		int pos = 0;
		int bitno = 0;
		int charSetLength = characterSet.length();

		int maxBitCnt = (int) Math.ceil(Math.log(charSetLength) / Math.log(2));

		for (int i = 0; (i < maxPwLength)
				&& ((pos * 8 + bitno) < (digestLength * 8)); ++i) {
			int part = 0;
			int bitCnt = maxBitCnt;
			int actPos = pos;
			int actBitno = bitno;

			int j = 0;

			for (; (j < bitCnt)
					&& ((actPos * 8 + actBitno) < (digestLength * 8)); ++j) {
				part <<= 1;
				part |= ((digest[actPos] & (1 << actBitno)) != 0) ? 1 : 0;
				if (++actBitno >= 8) {
					++actPos;
					actBitno = 0;
				}
			}

			if (part >= charSetLength) {
				part >>= 1;
				--actBitno;
				if (actBitno < 0) {
					--actPos;
					actBitno = 7;
				}
			}

			bitno = actBitno;
			pos = actPos;

			sPassword = sPassword + characterSet.charAt(part);
		}

		return sPassword;
	}

	/**
	 * This method creates a hash of the given string using the given hash type.
	 * 
	 * @param hashType
	 *            The hash type with which the string should be hashed as
	 *            {@link EHashType}.
	 * @param string
	 *            The string that should be hashed as {@link String}.
	 * @return The hashed string as {@link String} or null if the given hash
	 *         type does not exist.
	 */
	private static byte[] createHash(EHashType hashType, String string) {
		MessageDigest md;

		try {
			md = MessageDigest.getInstance(hashType.getJavaHashType());
			md.reset();
			md.update(string.getBytes());
			byte[] byteResult = md.digest();

			return byteResult;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return null;
	}
}
