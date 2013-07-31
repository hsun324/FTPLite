package com.hsun324.ftplite.commands;

import com.hsun324.ftplite.FTPCommand;
import com.hsun324.ftplite.FTPResponse;
import com.hsun324.ftplite.FTPResult;
import com.hsun324.ftplite.FTPState;

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
