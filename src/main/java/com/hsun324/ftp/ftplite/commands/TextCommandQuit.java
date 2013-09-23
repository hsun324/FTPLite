package com.hsun324.ftp.ftplite.commands;

import java.io.IOException;

import com.hsun324.ftp.ftplite.FTPResponse;
import com.hsun324.ftp.ftplite.FTPResult;
import com.hsun324.ftp.ftplite.client.FTPInterface;
import com.hsun324.ftp.ftplite.client.FTPInterface.ClientState;

/**
 * This {@link Command} handles the QUIT command.
 * @author hsun324
 * @version 0.7
 */
public class TextCommandQuit extends TextCommand {
	@Override
	public String getCommandContent(FTPInterface inter) {
		return "QUIT";
	}
	@Override
	public boolean isValidContext(FTPInterface inter) {
		return inter.getClientState() == ClientState.READY;
	}
	@Override
	public FTPResult handleResponse(FTPInterface inter, FTPResponse response) {
		try {
			if (response.getCode() == 221) {
				inter.close();
				return FTPResult.SUCCEEDED;
			}
		} catch (IOException e) {
			// TODO: Exceptions
		}
		return FTPResult.FAILED;
	}
}
