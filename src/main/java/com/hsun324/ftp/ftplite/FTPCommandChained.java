package com.hsun324.ftp.ftplite;

import java.io.IOException;


/**
 * A class that represents a chain of related commands to be sent to the
 * FTP server by the client immediately after one another.
 * <p>
 * The current implementation of this class uses a blocking <code>execute</code>
 * method and therefore requires the use of <code>quitExecution</code> to
 * allow the command queue thread to successfully terminate.
 * @author hsun324
 * @version 0.7
 */
public class FTPCommandChained extends FTPCommand {
	/**
	 * The list of chained commands.
	 */
	private final FTPCommand[] commands;
	/**
	 * Flag indicating whether one failed command will prevent the others from
	 * executing.
	 */
	private final boolean shortCircuit;
	
	/**
	 * Flag indicating whether this is the last command in the chain.
	 */
	private boolean lastCommand = false;
	/**
	 * Flag indicating whether the current command has completed executing.
	 */
	private boolean commandDone = false;
	/**
	 * The currently executing command.
	 */
	private FTPCommand currentCommand = null;
	
	/**
	 * Flag indicating whether a quit has been requested.
	 */
	private boolean quitRequested = false;
	
	/**
	 * The running total of the results of the chained commands.
	 */
	private FTPResult totalResult = new FTPResult(true);
	/**
	 * Command execution semaphore.
	 */
	private Object commandSync = new Object();
	
	/**
	 * Create a <code>FTPCommandChained</code> with the provided commands that
	 * will short circuit, or quit after one failed command.
	 * @param commands the commands to chain
	 */
	public FTPCommandChained(FTPCommand... commands) {
		this(true, commands);
	}
	
	/**
	 * Create a <code>FTPCommandChained</code> with the provided commands that
	 * will have the short circuit behavior defined by shortCircuit.
	 * @param shortCircuit whether this chain should short circuit
	 * @param commands the commands to chain
	 */
	public FTPCommandChained(boolean shortCircuit, FTPCommand... commands) {
		this.commands = commands;
		this.shortCircuit = shortCircuit;
	}
	
	@Override
	public void execute(FTPState state) throws IOException {
		int length = commands.length;
		for (int i = 0; i < length; i++) {
			currentCommand = commands[i];
			lastCommand = i >= length - 1;
			
			currentCommand.execute(state);
			waitForResponse();
			commandDone = false;
			if (quitRequested|| lastCommand) break;
		}
	}
	
	public void quitExecution() {
		quitRequested = true;
		synchronized (commandSync) {
			commandSync.notifyAll();
		}
	}
	
	/**
	 * Waits for the current command to complete and respond to
	 * the execution loop.
	 * @throws IOException
	 */
	private void waitForResponse() throws IOException {
		synchronized (commandSync) {
			try {
				while (!commandDone && !quitRequested)
					commandSync.wait();
			} catch (InterruptedException e) {
				throw new IOException(e);
			}
		}
	}

	@Override
	public FTPResult handleResponse(FTPState state, FTPResponse response) {
		FTPResult result = currentCommand.handleResponse(state, response);
		if (result != null) {
			totalResult = totalResult.or(result);
			if (shortCircuit && !totalResult.isSuccessful()) lastCommand = true;
			synchronized (commandSync) {
				commandDone = true;
				commandSync.notify();
			}
			if (lastCommand) return totalResult;
		}
		return null;
	}
}
