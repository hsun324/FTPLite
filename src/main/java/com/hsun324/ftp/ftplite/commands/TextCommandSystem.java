package com.hsun324.ftp.ftplite.commands;

import com.hsun324.ftp.ftplite.FTPResponse;
import com.hsun324.ftp.ftplite.FTPResult;
import com.hsun324.ftp.ftplite.client.FTPInterface;
import com.hsun324.ftp.ftplite.client.FTPInterface.ClientState;

/**
 * This {@link Command} handles the server
 * system SYST command.
 * @author hsun324
 * @version 0.7
 */
public class TextCommandSystem extends TextCommand {
	@Override
	public String getCommandContent(FTPInterface inter) {
		return "SYST";
	}
	@Override
	public boolean isValidContext(FTPInterface inter) {
		return inter.getClientState() == ClientState.READY;
	}
	@Override
	public FTPResult handleResponse(FTPInterface inter, FTPResponse response) {
		if (response.getCode() == 215) {
			return FTPResult.SUCCEEDED;
		}
		return FTPResult.FAILED;
	}
}
