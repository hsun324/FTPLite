package com.hsun324.ftplite;

public class FTPResult {
	public static final FTPResult SUCCEEDED;
	public static final FTPResult FAILED;
	
	static {
		SUCCEEDED = new FTPResult(true);
		FAILED = new FTPResult(false);
	}
	public FTPResult(boolean success) {
		this(success, null);
	}
	public FTPResult(boolean success, byte[] data) {
		this.success = success;
		this.data = data;
	}

	protected final boolean success;
	protected final byte[] data;

	public boolean isSuccessful() {
		return success;
	}
	public byte[] getData() {
		return data;
	}
	
	public FTPResult or(FTPResult result) {
		return new FTPResult(
			success && result.success,
			result.data != null ? result.data : data
		);
	}
}
