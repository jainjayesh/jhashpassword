package de.janbusch.jhashpassword.xml;


import java.io.File;


import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import de.janbusch.jhashpassword.xml.simple.HashPassword;

public class SimpleXMLUtil {

	private SimpleXMLUtil() {
		// Not used
	}

	public static HashPassword getXML(String path) throws Exception {
		HashPassword hashPassword = null;
		File hashpasswordXMLFile = new File(path);

		Serializer serializer = new Persister();
		hashPassword = serializer.read(HashPassword.class, hashpasswordXMLFile);
		hashPassword.sort();
		
		return hashPassword;
	}

	public static void writeXML(HashPassword hashPassword, String path) throws Exception {
		File hashpasswordXMLFile = new File(path);
		Serializer serializer = new Persister();
		serializer.write(hashPassword, hashpasswordXMLFile);
	}

}
