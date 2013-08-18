package com.hsun324.ftp.ftplite.commands;

import com.hsun324.ftp.ftplite.FTPCommand;
import com.hsun324.ftp.ftplite.FTPResponse;
import com.hsun324.ftp.ftplite.FTPResult;
import com.hsun324.ftp.ftplite.FTPState;

/**
 * This {@link FTPCommand} handles the QUIT command.
 * @author hsun324
 * @version 0.6a
 * @since 0.5
 */
public class FTPCommandQuit extends FTPCommand {
	@Override
	public String getCommandContent(FTPState state) {
		return "QUIT";
	}
	@Override
	public boolean isValidContext(FTPState state) {
		return state.authCompleted;
	}
	@Override
	public FTPResult handleResponse(FTPState state, FTPResponse response) {
		if (response.getCode() == 221) {
			state.client.close();
			return FTPResult.SUCCEEDED;
		}
		return FTPResult.FAILED;
	}
}
