package com.hsun324.ftp.ftplite;

import java.io.IOException;

/**
 * An abstract class that represents commands sent to the FTP server
 * by the client.
 * <p>
 * Subclasses are limited to implementing and overriding non-sensitive
 * functions of this class.
 * @author hsun324
 * @version 0.7
 */
public abstract class FTPCommand {
	/**
	 * A command execution semaphore.
	 */
	protected final Object executionSync = new Object();
	
	/**
	 * Flag for execution.
	 */
	private boolean executed = false;
	/**
	 * Sets this command as already executed.
	 */
	public final void setExecuted() {
		executed = true;
	}
	/**
	 * Gets whether this command is executed.
	 * @return execution flag
	 */
	public final boolean isExecuted() {
		return executed;
	}
	
	/**
	 * Causes this command to be executed after sanity checks. 
	 * @param state the current client state
	 * @throws IOException
	 */
	public final void push(FTPState state) throws IOException {
		if (!canBeReused() && isExecuted()) throw new IllegalStateException();
		execute(state);
		if (!canBeReused()) setExecuted();
	}

	/**
	 * Gets whether this command can be executed multiple times.
	 * @return reusability
	 */
	public boolean canBeReused() {
		return false;
	}

	/**
	 * Executes this command.
	 * <p>
	 * The default implementation takes the text given by the <code>getCommandContent(FTPState)</code>
	 * method and outputs it to the client command stream followed by a newline in the ASCII charset.
	 * @param state the current client state
	 * @throws IOException
	 */
	public void execute(FTPState state) throws IOException {
		StringBuilder builder = new StringBuilder(getCommandContent(state));
		System.out.println("> " + builder.toString());
		state.client.getCommandStream().write(builder.append(FTPCharset.NEWLINE).toString().getBytes(FTPCharset.ASCII));
	}
	
	/**
	 * Gets the content of the command to be sent to the server.
	 * <p>
	 * This method is used by the default <code>execute</code> methods
	 * in <code>FTPCommand</code>, {@link FTPDownloadCommand},
	 * and {@link FTPUploadCommand}.
	 * @param state the current client state
	 * @return the command content
	 */
	public String getCommandContent(FTPState state) {
		return "NOOP";
	}
	
	/**
	 * A method that signals the command to quit it's execution.
	 * <p>
	 * This method is used for terminating blocking command execution.
	 * It does nothing in it's default implementation.
	 */
	public void quitExecution() { }
	
	/**
	 * Gets whether the provided state is a valid context to execute this command in.
	 * @param state the current client state
	 * @return whether context is suitable
	 */
	public boolean isValidContext(FTPState state) {
		return true;
	}
	
	/**
	 * Causes the command to execute and handles the result.
	 * @param state the current client state
	 * @param response the server response
	 * @return whether the command is completed
	 */
	public final boolean pushResponse(FTPState state, FTPResponse response) {
		synchronized (executionSync) {
			FTPFuture future = state.currentFuture;
			if (future == null) return false;
			
			FTPResult result = handleResponse(state, response);
			if (result != null) {
				if (!future.isResultSet()) future.setResult(result);
				return true;
			}
			return false;
		}
	}
	
	/**
	 * Handles the provided response under the provided state.
	 * <p>
	 * If this command returns any {@link FTPResult} that is not
	 * null, it will be interpreted as the command being completed.
	 * Subclasses of <code>FTPCommand</code> should implement this
	 * method.
	 * @param state the current client state
	 * @param response the server response
	 * @return a result to the response
	 */
	public abstract FTPResult handleResponse(FTPState state, FTPResponse response);
}
