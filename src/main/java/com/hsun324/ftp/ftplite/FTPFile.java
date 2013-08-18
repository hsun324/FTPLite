package com.hsun324.ftp.ftplite;

import java.nio.charset.Charset;

/**
 * A class representing files downloaded and uploaded to and from the
 * FTP server.
 * <p>
 * This class abstracts the difference between Binary and ASCII type
 * FTP files and provides access two both forms of a file.
 * @author hsun324
 * @version 0.7
 */
public class FTPFile {
	/**
	 * Flag indicating whether this file was originally binary.
	 */
	private final boolean isBinary;
	/**
	 * The byte array source of this file.
	 */
	private final byte[] source;
	/**
	 * The text of this file.
	 */
	private final String text;
	/**
	 * The current text encoding.
	 */
	private final Charset encoding;

	/**
	 * Creates a binary <code>FTPFile</code> from
	 * the data in the byte array.
	 * @param data file data
	 */
	public FTPFile (byte[] data) {
		this.isBinary = true;
		this.source = data;
		
		this.text = new String(data, FTPCharset.ASCII);
		this.encoding = FTPCharset.ASCII;
	}
	/**
	 * Creates a text <code>FTPFile</code> from
	 * the data in the text with a charset specified by
	 * encoding.
	 * @param text file text
	 * @param encoding the file encoding
	 */
	public FTPFile (String text, Charset encoding) {
		this.isBinary = false;
		this.text = text;
		this.encoding = encoding;
		
		this.source = text.getBytes(encoding);
	}
	/**
	 * Creates a text <code>FTPFile</code> from
	 * the data in the byte array using a charset
	 * provided by encoding.
	 * @param data file data
	 * @param encoding the file encoding
	 */
	public FTPFile(byte[] data, Charset encoding) {
		this.isBinary = false;
		this.text = new String(data, encoding).replaceAll("(\n|\r)\r\n", "\r\n");
		this.encoding = encoding;
		
		this.source = data;
	}
	
	/**
	 * Gets whether this file was originally binary.
	 * @return binary
	 */
	public boolean isBinary() {
		return isBinary;
	}
	/**
	 * Gets whether this file was originally text.
	 * @return text
	 */
	public boolean isText() {
		return !isBinary;
	}
	/**
	 * Gets the source bytes.
	 * @return source bytes
	 */
	public byte[] getBytes() {
		return source;
	}
	/**
	 * Gets the text.
	 * @return file text
	 */
	public String getText() {
		return text;
	}
	/**
	 * Gets the encoding.
	 * @return file encoding
	 */
	public Charset getEncoding() {
		return encoding;
	}
}
