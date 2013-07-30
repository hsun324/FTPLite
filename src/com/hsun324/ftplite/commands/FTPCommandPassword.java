package com.hsun324.ftplite.commands;

import com.hsun324.ftplite.FTPCommand;
import com.hsun324.ftplite.FTPResponse;
import com.hsun324.ftplite.FTPResult;
import com.hsun324.ftplite.FTPState;

public class FTPCommandPassword extends FTPCommand {
	protected final String command;
	public FTPCommandPassword(String password) {
		this.command = "PASS " + password;
	}
	
	@Override
	public String getCommandContent() {
		return command;
	}
	@Override
	public boolean isValidContext(FTPState state) {
		return !state.authCompleted && state.authPassword;
	}
	@Override
	public FTPResult handleResponse(FTPState state, FTPResponse response) {
		if (response.getCode() == 230) state.authCompleted = true;
		else return FTPResult.FAILED;
		return FTPResult.SUCCEEDED;
	}
}
