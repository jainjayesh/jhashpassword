package de.janbusch.jhashpassword.net;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;

public class Util {
	public static String getMacAddress(InetAddress address) {
		try {
			String macAddress = new String();
			NetworkInterface ni;

			ni = NetworkInterface.getByInetAddress(address);

			if (ni != null) {
				byte[] mac;
				mac = ni.getHardwareAddress();
				if (mac != null) {
					for (int j = 0; j < mac.length; j++) {
						macAddress += String.format("%02X%s", mac[j],
								(j < mac.length - 1) ? "-" : "");
					}
				} else {
					macAddress = "Address doesn't exist or is not "
							+ "accessible.";
				}
			} else {
				macAddress = "Network Interface for the specified "
						+ "address is not found.";
			}

			return macAddress;
		} catch (SocketException e) {
			e.printStackTrace();
		}

		return "N/A";
	}

	public static String getOperatingSystem() {
		String nameOS = "os.name";
		String versionOS = "os.version";
		String architectureOS = "os.arch";

		return System.getProperty(nameOS) + "/" + System.getProperty(versionOS)
				+ "/" + System.getProperty(architectureOS);
	}

}
