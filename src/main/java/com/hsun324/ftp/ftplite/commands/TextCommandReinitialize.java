package com.hsun324.ftp.ftplite.commands;

import com.hsun324.ftp.ftplite.FTPResponse;
import com.hsun324.ftp.ftplite.FTPResult;
import com.hsun324.ftp.ftplite.client.FTPInterface;
import com.hsun324.ftp.ftplite.client.FTPInterface.ClientState;

/**
 * This {@link Command} handles the session
 * restart REIN command.
 * @author hsun324
 * @version 0.7
 */
public class TextCommandReinitialize extends TextCommand {
	@Override
	public String getCommandContent(FTPInterface inter) {
		return "REIN";
	}
	@Override
	public boolean isValidContext(FTPInterface inter) {
		return inter.getClientState() == ClientState.READY;
	}
	@Override
	public FTPResult handleResponse(FTPInterface inter, FTPResponse response) {
		if (response.getCode() == 220) {
			inter.setClientState(ClientState.REQUESTING_USERNAME);
			return FTPResult.SUCCEEDED;
		}
		return FTPResult.FAILED;
	}
}
