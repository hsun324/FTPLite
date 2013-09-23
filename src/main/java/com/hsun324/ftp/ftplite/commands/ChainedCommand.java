package com.hsun324.ftp.ftplite.commands;

import java.io.IOException;

import com.hsun324.ftp.ftplite.FTPResponse;
import com.hsun324.ftp.ftplite.FTPResult;
import com.hsun324.ftp.ftplite.client.FTPInterface;


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
public class ChainedCommand extends Command {
	/**
	 * The list of chained commands.
	 */
	private final Command[] commands;
	
	private final boolean isShortCircuit;
	
	private Command currentCommand = null;
	private int currentCommandIndex = 0;

	private FTPResult totalResult = new FTPResult(true);
	
	/**
	 * Create a <code>FTPCommandChained</code> with the provided commands that
	 * will short circuit, or quit after one failed command.
	 * @param commands the commands to chain
	 */
	public ChainedCommand(Command... commands) {
		this(true, commands);
	}
	
	/**
	 * Create a <code>FTPCommandChained</code> with the provided commands that
	 * will have the short circuit behavior defined by shortCircuit.
	 * @param isShortCircuit whether this chain should short circuit
	 * @param commands the commands to chain
	 */
	public ChainedCommand(boolean isShortCircuit, Command... commands) {
		this.commands = commands;
		this.isShortCircuit = isShortCircuit;
	}
	
	@Override
	public void execute(FTPInterface inter) throws IOException {
		if (commands.length == 0) return;
		
		currentCommand = commands[0];
		currentCommand.execute(inter);
	}

	@Override
	public FTPResult handleResponse(FTPInterface inter, FTPResponse response) {
		FTPResult result = currentCommand.handleResponse(inter, response);
		if (result != null) {
			totalResult = totalResult.or(result);
			
			++currentCommandIndex;
			
			if (isShortCircuit && !totalResult.isSuccessful()) return totalResult;
			if (currentCommandIndex >= commands.length) return totalResult;
			
			try {
				currentCommand = commands[currentCommandIndex];
				currentCommand.execute(inter);
			} catch (IOException e) {
				return totalResult.or(FTPResult.FAILED);
			}
		}
		return null;
	}
}
