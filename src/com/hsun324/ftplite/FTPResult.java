package com.hsun324.ftplite;

/**
 * This class represents a result from the execution of a
 * command. This class has an option data component that
 * represents the data collected by a command that
 * involves the transmission of data over the second FTP
 * channel.
 * <p>
 * <code>FTPResult</code>s can only be succeeded or failed
 * and do not contain any extra polarity information.
 * @author hsun324
 */
public class FTPResult {
	/**
	 * Constant succeeded <code>FTPResult</code>.
	 */
	public static final FTPResult SUCCEEDED = new FTPResult(true);
	/**
	 * Constant failed <code>FTPResult</code>.
	 */
	public static final FTPResult FAILED = new FTPResult(false);
	
	/**
	 * Creates a <code>FTPResult</code> with the specified success.
	 * @param success success
	 */
	public FTPResult(boolean success) {
		this(success, null);
	}
	/**
	 * Creates a <code>FTPResult</code> with the specified success and data.
	 * @param success success
	 * @param data command data
	 */
	public FTPResult(boolean success, byte[] data) {
		this.success = success;
		this.data = data;
	}

	/**
	 * Flag indicating the success of the command this <code>FTPResult</code>
	 * represents.
	 */
	protected final boolean success;
	/**
	 * The result data.
	 */
	protected final byte[] data;

	/**
	 * Gets whether this result represents success.
	 * @return success
	 */
	public boolean isSuccessful() {
		return success;
	}
	/**
	 * Gets this result's data.
	 * @return result data
	 */
	public byte[] getData() {
		return data;
	}
	
	/**
	 * Commands a logical "OR" operation between this
	 * object and the provided result.
	 * @param result the other result
	 * @return the or'ed result
	 */
	public FTPResult or(FTPResult result) {
		return new FTPResult(
			success && result.success,
			result.data != null ? result.data : data
		);
	}
}
