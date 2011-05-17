package de.janbusch.hashpassword.core;

/**
 * An enumeration that defines all hash types available in HashPassword.
 * 
 * @author Jan Busch
 * 
 */
public enum EHashType {
	MD5("MD5"), SHA1("SHA"), SHA256("SHA-256"), SHA384("SHA-384"), SHA512(
			"SHA-512");

	private String javaHashType;

	/**
	 * Constructor
	 * 
	 * @param javaHashType
	 */
	private EHashType(String javaHashType) {
		this.javaHashType = javaHashType;

	}

	/**
	 * Get the path for an image
	 * 
	 * @return path as String
	 */
	public String getJavaHashType() {
		return javaHashType;
	}
}
