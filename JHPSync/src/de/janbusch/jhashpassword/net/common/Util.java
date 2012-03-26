package de.janbusch.jhashpassword.net.common;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

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

	public static InetAddress getBroadcastAddress(InetAddress localHost)
			throws SocketException {
		Enumeration<NetworkInterface> interfaces = NetworkInterface
				.getNetworkInterfaces();
		while (interfaces.hasMoreElements()) {
			NetworkInterface networkInterface = interfaces.nextElement();
			if (networkInterface.isLoopback()) {
				continue; // Don't want to broadcast to the loopback interface
			}
			for (InterfaceAddress interfaceAddress : networkInterface
					.getInterfaceAddresses()) {
				InetAddress broadcast = interfaceAddress.getBroadcast();
				if (broadcast != null) {
					return broadcast;
				}
			}
		}
		return null;
	}

}
