package com.hsun324.ftplite.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hsun324.ftplite.FTPCommand;
import com.hsun324.ftplite.FTPResponse;
import com.hsun324.ftplite.FTPResult;
import com.hsun324.ftplite.FTPState;

public class FTPCommandPassive extends FTPCommand {
	
	private static final Pattern PASSIVE_REPONSE_PATTERN = Pattern.compile("[^(]*\\(((?:[0-9]{1,3},?){4}),([0-9]{1,3},[0-9]{1,3})\\)");
	private static final Pattern EXTENDED_PASSIVE_REPONSE_PATTERN = Pattern.compile("[^(]*\\((.)\\1{2}([0-9]{1,5})\\1\\)");

	public boolean canBeReused() {
		return true;
	}
	
	@Override
	public String getCommandContent(FTPState state) {
		if (state.featureExtPassive) return "EPSV";
		return "PASV";
	}
	@Override
	public boolean isValidContext(FTPState state) {
		return state.authCompleted && (state.modeActive || state.modeCommand == null);
	}
	@Override
	public FTPResult handleResponse(FTPState state, FTPResponse response) {
		String content = response.getContent();
		try {
			if (state.featureExtPassive) {
				Matcher matcher = EXTENDED_PASSIVE_REPONSE_PATTERN.matcher(content);
				if (matcher.find()) {
					state.dataHost = state.host;
					state.dataPort = Integer.parseInt(matcher.group(2));
					return FTPResult.SUCCEEDED;
				}
			} else {
				Matcher matcher = PASSIVE_REPONSE_PATTERN.matcher(content);
				if (matcher.find()) {
					state.dataHost = matcher.group(1).replace('.', ',');
					state.dataPort = Integer.parseInt(matcher.group(2)) * 256 + Integer.parseInt(matcher.group(3));
					return FTPResult.SUCCEEDED;
				}
			}
		} catch (NumberFormatException e) { }
		return FTPResult.FAILED;
	}
}
