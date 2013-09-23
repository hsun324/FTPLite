package com.hsun324.ftp.ftplite.commands;

import com.hsun324.ftp.ftplite.FTPObject;
import com.hsun324.ftp.ftplite.FTPResponse;
import com.hsun324.ftp.ftplite.client.FTPInterface;
import com.hsun324.ftp.ftplite.client.FTPInterface.ClientState;

/**
 * This {@link Command} handles the file
 * download RETR command.
 * @author hsun324
 * @version 0.7
 */
public class DownloadCommandRetrieveFile extends DownloadCommand {
	private final String command;
	public DownloadCommandRetrieveFile(FTPObject file) {
		// TODO: Current Directory / File Test
		if (file == null) throw new IllegalArgumentException();
		this.command = "RETR " + file.getPath();
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
	public byte[] processData(FTPInterface inter, FTPResponse response, byte[] data) {
		return data;
	}
}
