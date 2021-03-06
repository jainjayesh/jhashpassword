package de.janbusch.hashpassword.core;

import java.io.File;

public class CoreInformation {
	public static final String HASH_VERSION = "1.2";

	public static final String HASHPASSWORD_COPYRIGHT = "HashPassword\nCopyright 2004-2007 by JayBus\nInfinite Software Solutions\nhttp://www.fam-busch.de/html/hashpassword.html/";
	public static final String JHASHPASSWORD_COPYRIGHT = "JHashPassword\nCopyright 2009-2012 by Jan Busch\nhttp://iss0splace.wordpress.com/";
	public static final String ICONSET_COPYRIGHT = "Icons from the Crystal Clear icon set by Everaldo Coelho\n(http://www.everaldo.com/). The icons are licensed under the\nGNU Lesser General Public License (LGPL).";

	public static final String DEFAULT_CHARACTERSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_0123456789!\"$%&/()=?+#-.,:;";
	public static final String DEFAULT_PASSWORD_LENGTH = "32";
	public static final String HASH_PASSWORD_XML = "HashPassword.xml";
	public static final String HASH_PASSWORD_CONFIG_XML = "Config.xml";

	public static final String QRCODEFILE = System
			.getProperty("java.io.tmpdir")
			+ File.separatorChar
			+ "jhp_qrcode.png";

	public static String DEFAULT_HASHTYPE = EHashType.SHA1.toString();
}
