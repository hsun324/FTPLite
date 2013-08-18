package com.hsun324.ftp.ftplite;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A convenience class that contains miscellaneous methods
 * that are not specific to single functions within the
 * library.
 * @author hsun324
 * @version 0.6a
 */
public class FTPUtilities {
	private FTPUtilities() {}

	/**
	 * The pattern for retrieving filenames from paths. This pattern is very lenient on path format. The separator character is a "/".
	 */
	private static final Pattern FILENAME_REGEX_PATTERN = Pattern.compile("^(?:[^/.]+/)*([^/]*(?:\\.(.*))?)$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	/**
	 * The pattern for retrieving file extensions from filenames. This pattern is very lenient and will just take the last text after the last period.
	 */
	private static final Pattern FILEEXT_REGEX_PATTERN = Pattern.compile("^[^/]*(?:\\.(.*))?$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	
	/**
	 * Reads all the available bytes in the provided stream to a byte array.
	 * <p>
	 * The implementation of this method will cause a temporary memory use
	 * of up to the equivalent of three times the size of the resulting array.
	 * @param stream the stream to read
	 * @return the read bytes
	 * @throws IOException
	 */
	public static byte[] readAll(InputStream stream) throws IOException {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[100];
		int len = 0;
		while((len = stream.read(buffer)) != -1)
			outStream.write(buffer, 0, len);
		return outStream.toByteArray();
	}
	
	/**
	 * Opens a stream for downloading data from the server.
	 * @param modeCommand the current mode command
	 * @param host the client host
	 * @param port the client port
	 * @return the stream
	 * @throws IOException
	 */
	public static InputStream openTransferStream(FTPModeCommand modeCommand, String host, int port) throws IOException {
		if (modeCommand.isActive()) return new ServerSocket(port).accept().getInputStream();
		else return new Socket(host, port).getInputStream();
	}
	
	/**
	 * Opens a stream for uploading data to the server.
	 * @param modeCommand the current mode command
	 * @param host the client host
	 * @param port the client port
	 * @return the stream
	 * @throws IOException
	 */
	public static OutputStream openPushStream(FTPModeCommand modeCommand, String host, int port) throws IOException {
		if (modeCommand.isActive()) return new ServerSocket(port).accept().getOutputStream();
		else return new Socket(host, port).getOutputStream();
	}
	
	/**
	 * Gets a filename from a path using the filename pattern.
	 * @param path the path to use
	 * @return the filename
	 */
	public static String getFilename(String path) {
		if (path == null) return null;
		Matcher matcher = FILENAME_REGEX_PATTERN.matcher(path);
		if (matcher.find()) return matcher.group(2);
		return null;
	}
	/**
	 * Gets just the file's name part from a path using the file extension pattern.
	 * @param path the path to use
	 * @return the file's name
	 */
	public static String getName(String path) {
		String filename = getFilename(path);
		if (filename == null) return null;
		Matcher matcher = FILEEXT_REGEX_PATTERN.matcher(filename);
		if (matcher.find()) return matcher.group(1);
		return null;
	}
	/**
	 * Gets just the file's extension from a path using the file extension pattern.
	 * @param path the path to use
	 * @return the file's extension
	 */
	public static String getFileExtension(String path) {
		String filename = getFilename(path);
		if (filename == null) return null;
		Matcher matcher = FILEEXT_REGEX_PATTERN.matcher(filename);
		if (matcher.find()) return matcher.group(2);
		return null;
	}
}
