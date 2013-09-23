package com.hsun324.ftp.ftplite.commands;

import com.hsun324.ftp.ftplite.FTPResponse;
import com.hsun324.ftp.ftplite.FTPResult;
import com.hsun324.ftp.ftplite.client.FTPInterface;
import com.hsun324.ftp.ftplite.client.FTPInterface.ClientState;

/**
 * This {@link Command} handles the server welcome
 * message.
 * @author hsun324
 * @version 0.7
 */
public class TextCommandConnect extends Command {
	@Override
	public void execute(FTPInterface inter) { }
	@Override
	public FTPResult handleResponse(FTPInterface inter, FTPResponse response) {
		if (response.getCode() != 220) return FTPResult.FAILED;
		
		inter.setClientState(ClientState.REQUESTING_USERNAME);
		return FTPResult.SUCCEEDED;
	}
}
