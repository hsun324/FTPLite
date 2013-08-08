package com.hsun324.ftplite.commands;

import com.hsun324.ftplite.FTPCommand;
import com.hsun324.ftplite.FTPFilename;
import com.hsun324.ftplite.FTPResponse;
import com.hsun324.ftplite.FTPResult;
import com.hsun324.ftplite.FTPState;

/**
 * This {@link FTPCommand} handles the file
 * delete DELE command.
 * @author hsun324
 * @version 0.6a
 * @since 0.5
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
