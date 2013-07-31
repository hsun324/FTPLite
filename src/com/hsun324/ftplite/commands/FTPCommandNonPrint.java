package com.hsun324.ftplite.commands;

import com.hsun324.ftplite.FTPCommand;
import com.hsun324.ftplite.FTPResponse;
import com.hsun324.ftplite.FTPResult;
import com.hsun324.ftplite.FTPState;

public class FTPCommandNonPrint extends FTPCommand {
	public boolean canBeReused() {
		return true;
	}
	@Override
	public String getCommandContent(FTPState state) {
		return "TYPE N";
	}
	@Override
	public boolean isValidContext(FTPState state) {
		return state.authCompleted;
	}
	
	@Override
	public FTPResult handleResponse(FTPState state, FTPResponse response) {
		if (response.getCode() != 200) return FTPResult.FAILED;
		return FTPResult.SUCCEEDED;
	}
}
