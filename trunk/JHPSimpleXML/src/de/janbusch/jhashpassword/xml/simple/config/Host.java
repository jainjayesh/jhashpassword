package de.janbusch.jhashpassword.xml.simple.config;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root
public class Host implements Serializable {

	/**
	 * SerialVersionUID
	 */
	private static final long serialVersionUID = 2668237297568578451L;

	@Attribute(name = "IPAddress", required = true)
	private String ipAddress;
	@Attribute(name = "MACAddress", required = true)
	private String macAddress;
	@Attribute(name = "Code", required = true)
	private String code;

	@Override
	public boolean equals(Object otherHost) {
		if (otherHost instanceof Host) {
			if (((Host) otherHost).getMacAddress().compareTo(this.macAddress) == 0) {
				return true;
			}
		}

		return false;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}
