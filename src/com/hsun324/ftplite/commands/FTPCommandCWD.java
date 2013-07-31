package com.hsun324.ftplite.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hsun324.ftplite.FTPCommand;
import com.hsun324.ftplite.FTPFilename;
import com.hsun324.ftplite.FTPResponse;
import com.hsun324.ftplite.FTPResult;
import com.hsun324.ftplite.FTPState;

public class FTPCommandCWD extends FTPCommand {
	private static final Pattern CWD_RESPONSE_PATTERN = Pattern.compile("^\"([^\"]+)\"[^\"]*$");
	
	private final String command;
	
	public FTPCommandCWD(String command) {
		this.command = "CWD " + command;
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
		try {
			if (response.getCode() == 250) {
				Matcher matcher = CWD_RESPONSE_PATTERN.matcher(response.getContent());
				if (matcher.find()) {
					state.workingDirectory = new FTPFilename(state.workingDirectory, matcher.group(1));
					return FTPResult.SUCCEEDED;
				}
			}
		} catch (NumberFormatException e) { }
		return FTPResult.FAILED;
	}
}