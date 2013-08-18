package com.hsun324.ftp.ftplite.commands;

import com.hsun324.ftp.ftplite.FTPCommand;
import com.hsun324.ftp.ftplite.FTPFilename;
import com.hsun324.ftp.ftplite.FTPResponse;
import com.hsun324.ftp.ftplite.FTPResult;
import com.hsun324.ftp.ftplite.FTPState;

/**
 * This {@link FTPCommand} handles the file
 * delete DELE command.
 * @author hsun324
 * @version 0.7
 */
public class FTPCommandFileDelete extends FTPCommand {
	protected final String command;
	public FTPCommandFileDelete(FTPFilename file) {
		if (file == null) throw new IllegalArgumentException();
		this.command = "DELE " + file.getPath();
	}
	
	@Override
	public String getCommandContent(FTPState state) {
		return command;
	}
	@Override
	public boolean isValidContext(FTPState state) {
		return state.authCompleted;
	}
	@Override
	public FTPResult handleResponse(FTPState state, FTPResponse response) {
		if (response.getCode() == 250) return FTPResult.SUCCEEDED;
		return FTPResult.FAILED;
	}
}
