package com.hsun324.ftp.ftplite.commands;

import com.hsun324.ftp.ftplite.FTPCommand;
import com.hsun324.ftp.ftplite.FTPResponse;
import com.hsun324.ftp.ftplite.FTPResult;
import com.hsun324.ftp.ftplite.FTPState;

/**
 * This {@link FTPCommand} handles the server
 * system SYST command.
 * @author hsun324
 * @version 0.7
 */
public class FTPCommandSystem extends FTPCommand {
	@Override
	public String getCommandContent(FTPState state) {
		return "SYST";
	}
	@Override
	public boolean isValidContext(FTPState state) {
		return state.authCompleted;
	}
	@Override
	public FTPResult handleResponse(FTPState state, FTPResponse response) {
		if (response.getCode() == 215) {
			state.system = response.getContent();
			return FTPResult.SUCCEEDED;
		}
		return FTPResult.FAILED;
	}
}
