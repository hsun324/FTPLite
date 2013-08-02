package com.hsun324.ftplite;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FTPUtilities {
	private FTPUtilities() {}

	/**
	 * The pattern for retrieving filenames from paths. This pattern is very lenient on path format. The separator character is a "/".
	 */
	private static final Pattern FILENAME_REGEX_PATTERN = Pattern.compile("^(?:[^/.]+/)*([^/]*(?:\\.(.*))?)$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	private static final Pattern FILEEXT_REGEX_PATTERN = Pattern.compile("^[^/]*(?:\\.(.*))?$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	
	public static byte[] readAll(InputStream stream) throws IOException {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[100];
		int len = 0;
		while((len = stream.read(buffer)) != -1)
			outStream.write(buffer, 0, len);
		return outStream.toByteArray();
	}
	
	public static InputStream openTransferStream(boolean modeActive, String host, int port) throws IOException {
		if (modeActive) return new ServerSocket(port).accept().getInputStream();
		else return new Socket(host, port).getInputStream();
	}
	public static OutputStream openPushStream(boolean modeActive, String host, int port) throws IOException {
		if (modeActive) return new ServerSocket(port).accept().getOutputStream();
		else return new Socket(host, port).getOutputStream();
	}
	
	public static String getFilename(String path) {
		if (path == null) return null;
		Matcher matcher = FILENAME_REGEX_PATTERN.matcher(path);
		if (matcher.find()) return matcher.group(2);
		return null;
	}
	public static String getName(String path) {
		String filename = getFilename(path);
		if (filename == null) return null;
		Matcher matcher = FILEEXT_REGEX_PATTERN.matcher(filename);
		if (matcher.find()) return matcher.group(1);
		return null;
	}
	public static String getFileExtension(String path) {
		String filename = getFilename(path);
		if (filename == null) return null;
		Matcher matcher = FILEEXT_REGEX_PATTERN.matcher(filename);
		if (matcher.find()) return matcher.group(2);
		return null;
	}
}
