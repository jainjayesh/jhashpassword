package de.janbusch.jhashpassword.xml;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.content.Context;
import de.janbusch.hashpassword.core.CoreInformation;
import de.janbusch.jhashpassword.xml.simple.config.JHPConfig;
import de.janbusch.jhashpassword.xml.simple.data.HashPassword;

public class SimpleXMLUtil {

	private SimpleXMLUtil() {
		// Not used
	}

	public synchronized static HashPassword getXML(Context context) throws Exception {
		HashPassword hashPassword = null;
		FileInputStream hashpasswordXMLFile = context
				.openFileInput(CoreInformation.HASH_PASSWORD_XML);
		Serializer serializer = new Persister();
		hashPassword = serializer.read(HashPassword.class, hashpasswordXMLFile);

		return hashPassword;
	}

	public synchronized static void writeXML(HashPassword hashPassword, Context context)
			throws Exception {
		FileOutputStream hashpasswordXMLFile = context.openFileOutput(
				CoreInformation.HASH_PASSWORD_XML, Context.MODE_PRIVATE);
		Serializer serializer = new Persister();
		serializer.write(hashPassword, hashpasswordXMLFile);
	}

	public synchronized static JHPConfig getConfigXML(Context context) throws Exception {
		JHPConfig config = null;
		FileInputStream configXMLFile = context
				.openFileInput(CoreInformation.HASH_PASSWORD_CONFIG_XML);

		Serializer serializer = new Persister();
		config = serializer.read(JHPConfig.class, configXMLFile);
		
		return config;
	}

	public synchronized static void writeConfigXML(JHPConfig config, Context context) throws Exception {
		FileOutputStream configXMLFile = context.openFileOutput(
				CoreInformation.HASH_PASSWORD_CONFIG_XML, Context.MODE_PRIVATE);
		Serializer serializer = new Persister();
		serializer.write(config, configXMLFile);
	}
}
