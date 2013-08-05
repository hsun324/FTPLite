package com.hsun324.ftplite;

import java.nio.charset.Charset;

public class FTPCharset {
	public static final Charset ASCII;
	static {
		if (Charset.isSupported("US-ASCII")) ASCII = Charset.forName("US-ASCII");
		else ASCII = Charset.defaultCharset();
	}
	public static final Object NEWLINE = "\r\n";
}
