package com.hsun324.ftp.ftplite;

import java.io.IOException;

import com.hsun324.ftp.ftplite.client.FTPClient;
import com.hsun324.ftp.ftplite.client.FTPInterface;
import com.hsun324.ftp.ftplite.commands.Command;

/**
 * A class that represents a result that will be provided at an unspecified
 * time in the future. This class also acts as a wrapper for state and 
 * command execution.
 * <p>
 * This class is synchronized across threads. Methods in this class that retrieve
 * the result will block until the result is set.
 * @author hsun324
 * @version 0.7
 */
public final class FTPFuture {
	/**
	 * Creates a <code>FTPFuture</code> 
	 * @param client
	 * @param command
	 */
	public FTPFuture(FTPClient client, FTPInterface inter, Command command) {
		this.client = client;
		this.inter = inter;
		this.command = command;
	}
	
	/**
	 * Result set semaphore.
	 */
	protected final Object resultSync = new Object();
	
	/**
	 * This future's client.
	 */
	protected final FTPClient client;
	/**
	 * This future's client state.
	 */
	protected final FTPInterface inter;
	/**
	 * This future's command.
	 */
	protected final Command command;
	/**
	 * This future's command's result.
	 */
	protected FTPResult result;
	
	/**
	 * Executes the command represented by this future.
	 * @throws IOException
	 */
	public synchronized void execute() throws IOException {
		if (command.isValidContext(inter)) {
			command.execute(inter);
		} else result = FTPResult.FAILED;
	}
	
	/**
	 * Waits for a result and the returns it.
	 * @return the result
	 * @throws IOException
	 */
	public FTPResult getResult() throws IOException {
		waitUntilResult();
		return result;
	}
	
	public boolean completed() {
		synchronized (resultSync) {
			return result != null;
		}
	}
	
	/**
	 * Waits for the result to be set.
	 * @throws IOException
	 */
	public void waitUntilResult() throws IOException {
		synchronized (resultSync) {
			try {
				while (result == null)
					resultSync.wait();
			} catch (InterruptedException e) {
				throw new IOException(e);
			}
		}
	}
	/**
	 * Sets the result for this future.
	 * <p>
	 * If the result is already set, then this method will
	 * throw a {@link IllegalStateException}.
	 * @param result the result to set
	 */
	public void setResult(FTPResult result) {
		if (result == null) return;
		synchronized (resultSync) {
			if (this.result != null) throw new IllegalStateException();
			this.result = result;
			resultSync.notifyAll();
		}
	}

	/**
	 * Gets whether the result is already set.
	 * @return whether the result is set
	 */
	public boolean isResultSet() {
		return result != null;
	}

	public void quitExecution() {
		command.quitExecution();
	}

	public boolean pushResponse(FTPResponse response) {
		return command.processResponse(inter, response);
	}

}
