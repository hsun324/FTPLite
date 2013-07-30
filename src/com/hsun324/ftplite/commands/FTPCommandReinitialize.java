package com.hsun324.ftplite.commands;

import com.hsun324.ftplite.FTPCommand;
import com.hsun324.ftplite.FTPResponse;
import com.hsun324.ftplite.FTPResult;
import com.hsun324.ftplite.FTPState;

public class FTPCommandReinitialize extends FTPCommand {
	@Override
	public String getCommandContent() {
		return "REIN";
	}
	@Override
	public boolean isValidContext(FTPState state) {
		return state.authCompleted;
	}
	@Override
	public FTPResult handleResponse(FTPState state, FTPResponse response) {
		if (response.getCode() == 220) {
			state.reset();
			return FTPResult.SUCCEEDED;
		}
		return FTPResult.FAILED;
	}
}
