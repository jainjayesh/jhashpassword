package de.janbusch.hashpassword.core;

public class DRMUtil {

	private static final int SER_CODE_LENGTH = 16;
	private static final String SERIAL = "SERIAL";
	private static final EHashType HASHTYPE = EHashType.SHA512;
	private static final int ACT_CODE_LENGTH = 16;
	private static final String ACT_CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	private static final String SER_CHARSET = "0123456789";
	private static final String ACTIVATION = "ACTIVATION";

	public static String getActivationCode(String appName, String key) {
		String actCode = HashUtil.generatePassword(appName, ACTIVATION, key,
				HASHTYPE, ACT_CHARSET, ACT_CODE_LENGTH);
		return actCode;
	}
	
	public static String getSerialNo(String appName, String actCode) {
		String serialNo = HashUtil.generatePassword(appName, SERIAL, actCode,
				HASHTYPE, SER_CHARSET, SER_CODE_LENGTH);
		return serialNo;
	}
	
	public static boolean validateSerialNo(String appName, String key, String pendingSerial) {
		String actCode = getActivationCode(appName, key);
		String actualSerial = getSerialNo(appName, actCode);
		
		if(actualSerial.matches(pendingSerial)){
			return true;
		} else {
			return false;
		}
	}

}
