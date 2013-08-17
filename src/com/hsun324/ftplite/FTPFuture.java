package com.hsun324.ftplite;

import java.io.IOException;

import com.hsun324.ftplite.client.FTPClient;

/**
 * A class that represents a result that will be provided at an unspecified
 * time in the future. This class also acts as a wrapper for state and 
 * command execution.
 * <p>
 * This class is synchronized across threads. Methods in this class that retrieve
 * the result will block until the result is set.
 * @author hsun324
 * @version 0.6a
 * @since 0.5
 */
public final class FTPFuture {
	/**
	 * Creates a <code>FTPFuture</code> 
	 * @param client
	 * @param command
	 */
	public FTPFuture(FTPClient client, FTPState state, FTPCommand command) {
		this.client = client;
		this.state = state;
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
	protected final FTPState state;
	/**
	 * This future's command.
	 */
	protected final FTPCommand command;
	/**
	 * This future's command's result.
	 */
	protected FTPResult result;
	
	/**
	 * Executes the command represented by this future.
	 * @throws IOException
	 */
	public synchronized void execute() throws IOException {
		if (command.isValidContext(state)) {
			command.execute(state);
		} else result = FTPResult.FAILED;
		command.setExecuted();
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
	 * @since 0.6a
	 */
	public boolean isResultSet() {
		return result != null;
	}

	public void quitExecution() {
		command.quitExecution();
	}

	public boolean pushResponse(FTPResponse response) {
		return command.pushResponse(state, response);
	}

}
