package com.hsun324.ftplite.commands;

import com.hsun324.ftplite.FTPCommand;
import com.hsun324.ftplite.FTPDownloadCommand;
import com.hsun324.ftplite.FTPFilename;
import com.hsun324.ftplite.FTPResponse;
import com.hsun324.ftplite.FTPState;

/**
 * This {@link FTPCommand} handles the file
 * download RETR command.
 * @author hsun324
 * @version 0.6a
 * @since 0.6a
 */
public class FTPCommandFileRetrieve extends FTPDownloadCommand {
	private final String command;
	public FTPCommandFileRetrieve(FTPFilename file) {
		// TODO: Current Directory / File Test
		if (file == null) throw new IllegalArgumentException();
		this.command = "RETR " + file.getPath();
	}
	
	@Override
	public String getCommandContent(FTPState state) {
		return command;
	}
	@Override
	public boolean isValidContext(FTPState state) {
		return state.authCompleted && state.modeCommand != null;
	}

	@Override
	public byte[] processData(FTPState state, FTPResponse response, byte[] data) {
		return data;
	}
}
