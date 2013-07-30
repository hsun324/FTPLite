package com.hsun324.ftplite.commands;

import com.hsun324.ftplite.FTPCommand;
import com.hsun324.ftplite.FTPResponse;
import com.hsun324.ftplite.FTPResult;
import com.hsun324.ftplite.FTPState;

public class FTPCommandPassive extends FTPCommand {
	@Override
	public String getCommandContent() {
		return "PASV";
	}
	@Override
	public boolean isValidContext(FTPState state) {
		return state.authCompleted && (state.modeActive || state.modeCommand == null);
	}
	@Override
	public FTPResult handleResponse(FTPState state, FTPResponse response) {
		String content = response.getDescription();
		int start = content.indexOf('(');
		int end = content.indexOf(')');
		if (start > -1 && end > -1) {
			if (response.getCode() == 227) {
				String[] params = content.substring(start + 1, end).split(",");
				if (params.length == 6) {
					state.dataHost = params[0] + "." + params[1] + "." + params[2] + "." + params[3];
					state.dataPort = Integer.parseInt(params[4]) * 256 + Integer.parseInt(params[5]);
					return FTPResult.SUCCEEDED;
				}
			}
		}
		return FTPResult.FAILED;
	}
}
