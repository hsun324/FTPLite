package com.hsun324.ftp.ftplite.commands;

import com.hsun324.ftp.ftplite.FTPResponse;
import com.hsun324.ftp.ftplite.FTPResult;
import com.hsun324.ftp.ftplite.client.FTPInterface;
import com.hsun324.ftp.ftplite.client.FTPInterface.ClientState;

/**
 * This {@link Command} handles the username
 * USER command.
 * @author hsun324
 * @version 0.7
 */
public class TextCommandUser extends TextCommand {
	protected final String command;
	public TextCommandUser(String username) {
		this.command = "USER " + username;
	}
	
	@Override
	public String getCommandContent(FTPInterface inter) {
		return command;
	}
	@Override
	public boolean isValidContext(FTPInterface inter) {
		return inter.getClientState() == ClientState.READY;
	}
	@Override
	public FTPResult handleResponse(FTPInterface inter, FTPResponse response) {
		if (response.getCode() == 331) inter.setClientState(ClientState.REQUESTING_PASSWORD);
		else if (response.getCode() == 230) inter.setClientState(ClientState.READY);
		else return FTPResult.FAILED;
		
		return FTPResult.SUCCEEDED;
	}
}
