package com.hsun324.ftp.ftplite.commands;

import java.io.IOException;

import com.hsun324.ftp.ftplite.FTPFuture;
import com.hsun324.ftp.ftplite.FTPResponse;
import com.hsun324.ftp.ftplite.FTPResult;
import com.hsun324.ftp.ftplite.client.FTPInterface;

/**
 * An abstract class that represents commands sent to the FTP server
 * by the client.
 * <p>
 * Subclasses are limited to implementing and overriding non-sensitive
 * functions of this class.
 * @author hsun324
 * @version 0.7
 */
public abstract class Command {
	public synchronized final void run(FTPInterface inter) throws IOException {
		execute(inter);
	}

	/**
	 * Executes this command.
	 * <p>
	 * The default implementation takes the text given by the <code>getCommandContent(FTPState)</code>
	 * method and outputs it to the client command stream followed by a newline in the ASCII charset.
	 * @param state the current client state
	 * @throws IOException
	 */
	public abstract void execute(FTPInterface inter) throws IOException;
	
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
	public boolean isValidContext(FTPInterface inter) {
		return true;
	}
	
	/**
	 * Causes the command to execute and handles the result.
	 * @param state the current client state
	 * @param response the server response
	 * @return whether the command is completed
	 */
	public synchronized final boolean processResponse(FTPInterface inter, FTPResponse response) {
		FTPFuture future = inter.getFuture();
		if (future == null) return false;
		
		FTPResult result = handleResponse(inter, response);
		if (result != null) {
			if (!future.isResultSet()) future.setResult(result);
			return true;
		}
		return false;
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
	public abstract FTPResult handleResponse(FTPInterface inter, FTPResponse response);
}
