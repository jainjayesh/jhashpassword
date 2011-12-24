package de.janbusch.jhashpassword.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

public class Util {
	public static InetAddress getBroadcastAddress(Context context)
			throws IOException {
		WifiManager wifi = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		DhcpInfo dhcp = wifi.getDhcpInfo();
		// handle null somehow

		int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
		byte[] quads = new byte[4];
		for (int k = 0; k < 4; k++)
			quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
		return InetAddress.getByAddress(quads);
	}

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

	public static String getOperatingSystemAndroid() {
		return "Android" + "/" + Build.VERSION.RELEASE;
	}

	public static String getMacAddressAndroid(Context context) {
		WifiManager wifi = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		
		return wifi.getConnectionInfo().getMacAddress();
	}
}
