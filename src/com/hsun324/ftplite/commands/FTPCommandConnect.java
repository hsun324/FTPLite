package com.hsun324.ftplite.commands;

import com.hsun324.ftplite.FTPCommand;
import com.hsun324.ftplite.FTPResponse;
import com.hsun324.ftplite.FTPResult;
import com.hsun324.ftplite.FTPState;

/**
 * This {@link FTPCommand} handles the server welcome
 * message.
 * @author hsun324
 * @version 0.6a
 */
public class FTPCommandConnect extends FTPCommand {
	@Override
	public void execute(FTPState state) { }
	@Override
	public FTPResult handleResponse(FTPState state, FTPResponse response) {
		if (response.getCode() != 220) return FTPResult.FAILED;
		
		state.reset();
		state.connected = true;
		state.welcomeMessage = response.getContent();
		return FTPResult.SUCCEEDED;
	}
}
