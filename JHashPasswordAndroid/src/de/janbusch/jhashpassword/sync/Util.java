package de.janbusch.jhashpassword.sync;

import java.io.IOException;
import java.net.InetAddress;

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

	public static String getOperatingSystemAndroid() {
		return "Android" + "/" + Build.VERSION.RELEASE;
	}

	public static String getMacAddressAndroid(Context context) {
		WifiManager wifi = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		
		return wifi.getConnectionInfo().getMacAddress();
	}
}
