package de.janbusch.jhashpassword.xml.simple.config;

import java.io.Serializable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;


/**
 * Java class for JHPConfig complex type.
 *  
 */
@Root(name = "JHPConfig", strict = false)
public class JHPConfig implements Serializable {

	/**
	 * SerialVersionUID
	 */
	private static final long serialVersionUID = 1712459181240180863L;

	public static final String jhpCXMLVersion = "1.0"; 
	
	@Element(name = "Synchronization", required = false)
	private Synchronization synchronization;

	/**
	 * Gets the value of the synchronisation property.
	 * 
	 * @return possible object is {@link Synchronization }
	 * 
	 */
	public Synchronization getSynchronization() {
		if (synchronization == null) {
			synchronization = new Synchronization();
		}
		return synchronization;
	}

	/**
	 * Sets the value of the synchronization property.
	 * 
	 * @param value
	 *            allowed object is {@link Synchronization }
	 * 
	 */
	public void setSynchronization(Synchronization value) {
		this.synchronization = value;
	}

}
