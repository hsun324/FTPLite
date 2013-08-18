package com.hsun324.ftp.ftplite.commands;

import java.io.IOException;

import com.hsun324.ftp.ftplite.FTPCommand;
import com.hsun324.ftp.ftplite.FTPResponse;
import com.hsun324.ftp.ftplite.FTPResult;
import com.hsun324.ftp.ftplite.FTPState;

/**
 * This {@link FTPCommand} handles the username
 * USER command.
 * @author hsun324
 * @version 0.6a
 */
public class FTPCommandUser extends FTPCommand {
	protected final String command;
	public FTPCommandUser(String username) {
		this.command = "USER " + username;
	}

	@Override
	public void execute(FTPState state) throws IOException {
		state.authStarted = true;
		super.execute(state);
	}
	
	@Override
	public String getCommandContent(FTPState state) {
		return command;
	}
	@Override
	public boolean isValidContext(FTPState state) {
		return !state.authCompleted && !state.authStarted;
	}
	@Override
	public FTPResult handleResponse(FTPState state, FTPResponse response) {
		if (response.getCode() == 331) state.authPassword = true;
		else if (response.getCode() == 230) state.authCompleted = true;
		else return FTPResult.FAILED;
		
		return FTPResult.SUCCEEDED;
	}
}
