package de.janbusch.hashpassword.core;

public class CoreInformation {
	public static final String HASH_VERSION = "1.2";

	public static final String HASHPASSWORD_COPYRIGHT = "HashPassword\nCopyright 2004-2007 by JayBus\nInfinite Software Solutions";
	public static final String JHASHPASSWORD_COPYRIGHT = "JHashPassword\nCopyright 2009-2011 by Jan Busch\nhttp://iss0splace.wordpress.com/";
	public static final String ICONSET_COPYRIGHT = "Icons from the Crystal Clear icon set by Everaldo Coelho\n(http://www.everaldo.com/). The icons are licensed under the\nGNU Lesser General Public License (LGPL).";

	public static final String DEFAULT_CHARACTERSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_0123456789!\"§$%&/()=?+#-.,:;";
	public static final String DEFAULT_PASSWORD_LENGTH = "32";
	public static final String HASH_PASSWORD_XML = "HashPassword.xml";
	public static final int SYNC_PORT = 1337;
	public static final int SYNC_TIMEOUT = 1000;
	public static String DEFAULT_HASHTYPE = EHashType.SHA1.toString();
}
