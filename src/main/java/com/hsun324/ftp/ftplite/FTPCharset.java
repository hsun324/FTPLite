package com.hsun324.ftp.ftplite;

import java.nio.charset.Charset;

/**
 * Convenience class that holds certain FTP protocol format objects.
 * @author hsun324
 * @version 0.6a
 * @since 0.6a
 */
public class FTPCharset {
	/**
	 * Convenience object of the US-ASCII charset used for FTP.
	 * @since 0.6a
	 */
	public static final Charset ASCII;
	
	static {
		if (Charset.isSupported("US-ASCII")) ASCII = Charset.forName("US-ASCII");
		else ASCII = Charset.defaultCharset();
	}
	
	/**
	 * Convenience object of the FTP newline.
	 * @since 0.6a
	 */
	public static final String NEWLINE = "\r\n";
}
