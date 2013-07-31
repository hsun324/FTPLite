package com.hsun324.ftplite.commands;

import com.hsun324.ftplite.FTPCommand;
import com.hsun324.ftplite.FTPResponse;
import com.hsun324.ftplite.FTPResult;
import com.hsun324.ftplite.FTPState;

public class FTPCommandUTF8 extends FTPCommand {
	@Override
	public String getCommandContent(FTPState state) {
		return "OPTS UTF-8";
	}
	@Override
	public boolean isValidContext(FTPState state) {
		return state.authCompleted;
	}
	@Override
	public FTPResult handleResponse(FTPState state, FTPResponse response) {
		if (response.getCode() == 200) state.acceptUTF8 = true;
		return FTPResult.SUCCEEDED;
	}
}
