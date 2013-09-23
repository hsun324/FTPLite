package com.hsun324.ftp.ftplite.commands;

import com.hsun324.ftp.ftplite.FTPObject;
import com.hsun324.ftp.ftplite.FTPResponse;
import com.hsun324.ftp.ftplite.FTPResult;
import com.hsun324.ftp.ftplite.client.FTPInterface;
import com.hsun324.ftp.ftplite.client.FTPInterface.ClientState;

/**
 * This {@link Command} handles the file
 * delete DELE command.
 * @author hsun324
 * @version 0.7
 */
public class TextCommandDeleteFile extends TextCommand {
	protected final String command;
	public TextCommandDeleteFile(FTPObject file) {
		if (file == null) throw new IllegalArgumentException();
		this.command = "DELE " + file.getPath();
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
		if (response.getCode() == 250) return FTPResult.SUCCEEDED;
		return FTPResult.FAILED;
	}
}
