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
	public static enum FTPFiletype {
		ASCII("US-ASCII"),
		TEXT("US-ASCII"),
		BINARY;
		
		private final Charset charset;
		private final boolean isBinary;
		private FTPFiletype() {
			this.isBinary = true;
			this.charset = null;
		}
		private FTPFiletype(String charsetName) {
			this.isBinary = false;
			this.charset = Charset.isSupported(charsetName) ? Charset.forName(charsetName) : Charset.defaultCharset();
		}
		
		public boolean isBinary() {
			return isBinary;
		}
		public Charset getCharset() {
			return charset;
		}
	}
	
	/**
	 * The file's type.
	 */
	private final FTPFiletype filetype;
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
		this.filetype = FTPFiletype.BINARY;
		this.source = data;
		
		this.encoding = FTPFiletype.ASCII.getCharset();
		this.text = new String(data, this.encoding);
	}
	/**
	 * Creates a text <code>FTPFile</code> from
	 * the data in the text with a charset specified by
	 * encoding.
	 * @param text file text
	 * @param encoding the file encoding
	 */
	public FTPFile (String text, Charset encoding) {
		this.filetype = FTPFiletype.TEXT;
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
		this.filetype = FTPFiletype.TEXT;
		this.text = new String(data, encoding).replaceAll("(\n|\r)\r\n", "\r\n");
		this.encoding = encoding;
		
		this.source = data;
	}
	
	/**
	 * Gets this file's file type.
	 * @return the file type
	 */
	public FTPFiletype getFiletype() {
		return filetype;
	}
	
	/**
	 * Gets whether this file was originally binary.
	 * @return binary
	 */
	public boolean isBinary() {
		return filetype.isBinary();
	}
	/**
	 * Gets whether this file was originally text.
	 * @return text
	 */
	public boolean isText() {
		return !filetype.isBinary();
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
