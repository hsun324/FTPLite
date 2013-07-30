package com.hsun324.ftplite;

public class FTPResponse {
	private final int code;
	private final String description;
	private final String rep;
	
	protected FTPResponse(int code, String description) {
		this.code = code;
		this.description = description;
		this.rep = code + " " + description;
	}
	
	public int getCode() {
		return code;
	}
	public String getDescription() {
		return description;
	}
	public String toString() {
		return rep;
	}
}
