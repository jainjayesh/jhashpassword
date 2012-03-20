package de.janbusch.jhashpassword.net.common;

import java.net.InetSocketAddress;

public class Partner {
	private InetSocketAddress myAddress;
	private String macAddress;
	private String operatingSystem;

	public Partner(InetSocketAddress myAddress, String macAddress,
			String operatingSystem) {
		this.myAddress = myAddress;
		this.macAddress = macAddress;
		this.operatingSystem = operatingSystem;
	}

	public void setMyAddress(InetSocketAddress myAddress) {
		this.myAddress = myAddress;
	}

	public InetSocketAddress getAddress() {
		return myAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setOperatingSystem(String operatingSystem) {
		this.operatingSystem = operatingSystem;
	}

	public String getOperatingSystem() {
		return operatingSystem;
	}

}
