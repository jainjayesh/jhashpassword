package de.janbusch.jhashpassword.xml;


import java.io.File;


import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import de.janbusch.hashpassword.core.CoreInformation;
import de.janbusch.jhashpassword.xml.simple.config.JHPConfig;
import de.janbusch.jhashpassword.xml.simple.data.HashPassword;

public class SimpleXMLUtil {

	private SimpleXMLUtil() {
		// Not used
	}

	public static HashPassword getXML() throws Exception {
		HashPassword hashPassword = null;
		File hashpasswordXMLFile = new File(CoreInformation.HASH_PASSWORD_XML);

		Serializer serializer = new Persister();
		hashPassword = serializer.read(HashPassword.class, hashpasswordXMLFile);
		hashPassword.sort();
		
		return hashPassword;
	}

	public static void writeXML(HashPassword hashPassword) throws Exception {
		File hashpasswordXMLFile = new File(CoreInformation.HASH_PASSWORD_XML);
		Serializer serializer = new Persister();
		serializer.write(hashPassword, hashpasswordXMLFile);
	}
	
	public static JHPConfig getConfigXML() throws Exception {
		JHPConfig config = null;
		File configXMLFile = new File(CoreInformation.HASH_PASSWORD_CONFIG_XML);

		Serializer serializer = new Persister();
		config = serializer.read(JHPConfig.class, configXMLFile);
		
		return config;
	}

	public static void writeConfigXML(JHPConfig config) throws Exception {
		File configXMLFile = new File(CoreInformation.HASH_PASSWORD_CONFIG_XML);
		Serializer serializer = new Persister();
		serializer.write(config, configXMLFile);
	}

}
