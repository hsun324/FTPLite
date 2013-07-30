package com.hsun324.ftplite;

public class FTPResponse {
	private final int code;
	private final String content;
	private final String rep;
	
	protected FTPResponse(int code, String content) {
		this.code = code;
		this.content = content;
		this.rep = code + " " + content;
	}
	
	public int getCode() {
		return code;
	}
	public String getContent() {
		return content;
	}
	public String toString() {
		return rep;
	}
}
