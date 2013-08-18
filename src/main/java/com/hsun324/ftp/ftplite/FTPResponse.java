package com.hsun324.ftp.ftplite;

/**
 * This class represents a response from the server including
 * a FTP response code and optionally multi-line content.
 * @author hsun324
 * @version 0.5
 */
public class FTPResponse {
	/**
	 * The response code.
	 */
	private final int code;
	/**
	 * The response content.
	 */
	private final String content;
	/**
	 * The combined response.
	 */
	private final String rep;
	
	/**
	 * Creates a <code>FTPResponse</code> with the provided code, and content.
	 * @param code response code
	 * @param content response content.
	 */
	public FTPResponse(int code, String content) {
		this.code = code;
		this.content = content;
		this.rep = code + " " + content;
	}
	
	/**
	 * Gets the response code.
	 * @return response code
	 */
	public int getCode() {
		return code;
	}
	/**
	 * Gets the response content.
	 * @return response content.
	 */
	public String getContent() {
		return content;
	}
	public String toString() {
		return rep;
	}
}
