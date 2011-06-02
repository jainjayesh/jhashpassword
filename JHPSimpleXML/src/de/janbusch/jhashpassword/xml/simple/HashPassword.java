package de.janbusch.jhashpassword.xml.simple;

import java.io.Serializable;
import java.math.BigInteger;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * <p>
 * Java class for HashPassword complex type.
 *  
 */
@Root(name = "HashPassword", strict = false)
public class HashPassword implements Serializable {

	/**
	 * SerialVersionUID
	 */
	private static final long serialVersionUID = 4420194836998361651L;
	public static final String jhpSXMLVersion = "2.3"; 
	
	@Element(name = "Hosts", required = false)
	private Hosts hosts;
	@Attribute(name = "Version", required = false)
	private BigInteger version;
	@Attribute(name = "DefaultHashType", required = true)
	private String defaultHashType;
	@Attribute(name = "DefaultCharset", required = true)
	private String defaultCharset;
	@Attribute(name = "DefaultPasswordLength", required = true)
	private String defaultPasswordLength;
	@Attribute(name = "LastHost", required = false)
	private String lastHost;
	@Attribute(name = "Timeout", required = false)
	private Integer timeout;

	/**
	 * Gets the value of the hosts property.
	 * 
	 * @return possible object is {@link Hosts }
	 * 
	 */
	public Hosts getHosts() {
		if (hosts == null) {
			hosts = new Hosts();
		}
		return hosts;
	}

	/**
	 * Sets the value of the hosts property.
	 * 
	 * @param value
	 *            allowed object is {@link Hosts }
	 * 
	 */
	public void setHosts(Hosts value) {
		this.hosts = value;
	}

	/**
	 * Gets the value of the version property.
	 * 
	 * @return possible object is {@link BigInteger }
	 * 
	 */
	public BigInteger getVersion() {
		return version;
	}

	/**
	 * Sets the value of the version property.
	 * 
	 * @param value
	 *            allowed object is {@link BigInteger }
	 * 
	 */
	public void setVersion(BigInteger value) {
		this.version = value;
	}

	/**
	 * Gets the value of the defaultHashType property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDefaultHashType() {
		return defaultHashType;
	}

	/**
	 * Sets the value of the defaultHashType property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDefaultHashType(String value) {
		this.defaultHashType = value;
	}

	/**
	 * Gets the value of the defaultCharset property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDefaultCharset() {
		return defaultCharset;
	}

	/**
	 * Sets the value of the defaultCharset property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDefaultCharset(String value) {
		this.defaultCharset = value;
	}

	/**
	 * Gets the value of the defaultPasswordLength property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDefaultPasswordLength() {
		return defaultPasswordLength;
	}

	/**
	 * Sets the value of the defaultPasswordLength property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDefaultPasswordLength(String value) {
		this.defaultPasswordLength = value;
	}

	/**
	 * Gets the value of the lastHost property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getLastHost() {
		if (lastHost == null) {
			lastHost = "Hostname";
		}
		return lastHost;
	}

	/**
	 * Sets the value of the lastHost property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setLastHost(String value) {
		this.lastHost = value;
	}

	/**
	 * Gets the value of the timeout property.
	 * 
	 * @return possible object is {@link Integer }
	 * 
	 */
	public Integer getTimeOut() {
		if(timeout == null) {
			timeout = 180000;
		}
		return timeout;
	}

	/**
	 * Sets the value of the timeout property.
	 * 
	 * @param value
	 *            allowed object is {@link Integer }
	 * 
	 */
	public void setTimeout(Integer value) {
		this.timeout = value;
	}

}
