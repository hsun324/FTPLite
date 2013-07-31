package com.hsun324.ftplite.commands;

import com.hsun324.ftplite.FTPCommand;
import com.hsun324.ftplite.FTPResponse;
import com.hsun324.ftplite.FTPResult;
import com.hsun324.ftplite.FTPState;

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
