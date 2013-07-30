package com.hsun324.ftplite.commands;

import java.io.IOException;

import com.hsun324.ftplite.FTPCommand;
import com.hsun324.ftplite.FTPResponse;
import com.hsun324.ftplite.FTPResult;
import com.hsun324.ftplite.FTPState;

public class FTPCommandChained extends FTPCommand {
	private final FTPCommand[] commands;
	private final boolean shortCircuit;
	
	private boolean lastCommand = false;
	private boolean commandDone = false;
	private FTPCommand currentCommand = null;
	
	private FTPResult totalResult = new FTPResult(true);
	
	private Object commandSync = new Object();
	
	public FTPCommandChained(FTPCommand... commands) {
		this(true, commands);
	}
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
			if (lastCommand) break;
		}
	}
	
	private void waitForResponse() throws IOException {
		synchronized (commandSync) {
			try {
				while (!commandDone)
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
