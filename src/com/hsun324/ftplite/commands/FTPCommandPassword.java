package com.hsun324.ftplite.commands;

import com.hsun324.ftplite.FTPCommand;
import com.hsun324.ftplite.FTPResponse;
import com.hsun324.ftplite.FTPResult;
import com.hsun324.ftplite.FTPState;

/**
 * This {@link FTPCommand} handles the password
 * PASS command.
 * @author hsun324
 * @version 0.6a
 */
public class FTPCommandPassword extends FTPCommand {
	protected final String command;
	public FTPCommandPassword(String password) {
		this.command = "PASS " + password;
	}
	
	@Override
	public String getCommandContent(FTPState state) {
		return command;
	}
	@Override
	public boolean isValidContext(FTPState state) {
		return !state.authCompleted && state.authPassword;
	}
	@Override
	public FTPResult handleResponse(FTPState state, FTPResponse response) {
		if (response.getCode() == 230) {
			state.authCompleted = true;
			return FTPResult.SUCCEEDED;
		}
		else {
			state.authStarted = false;
			state.authPassword = false;
			return FTPResult.FAILED;
		}
	}
}
