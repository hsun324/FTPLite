package com.hsun324.ftp.ftplite.commands;

import com.hsun324.ftp.ftplite.FTPResponse;
import com.hsun324.ftp.ftplite.FTPResult;
import com.hsun324.ftp.ftplite.client.FTPInterface;
import com.hsun324.ftp.ftplite.client.FTPInterface.ClientState;

/**
 * This {@link Command} handles the password
 * PASS command.
 * @author hsun324
 * @version 0.7
 */
public class TextCommandPassword extends TextCommand {
	protected final String command;
	public TextCommandPassword(String password) {
		this.command = "PASS " + password;
	}
	
	@Override
	public String getCommandContent(FTPInterface inter) {
		return command;
	}
	@Override
	public boolean isValidContext(FTPInterface inter) {
		return inter.getClientState() == ClientState.REQUESTING_PASSWORD;
	}
	@Override
	public FTPResult handleResponse(FTPInterface inter, FTPResponse response) {
		if (response.getCode() == 230) {
			inter.setClientState(ClientState.READY);
			return FTPResult.SUCCEEDED;
		}
		else {
			inter.setClientState(ClientState.REQUESTING_USERNAME);
			return FTPResult.FAILED;
		}
	}
}
