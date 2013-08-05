package com.hsun324.ftplite.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hsun324.ftplite.FTPCharset;
import com.hsun324.ftplite.FTPCommand;
import com.hsun324.ftplite.FTPFilename;
import com.hsun324.ftplite.FTPResponse;
import com.hsun324.ftplite.FTPResult;
import com.hsun324.ftplite.FTPState;

public class FTPCommandDirectory extends FTPCommand {
	private static final Pattern CWD_RESPONSE_PATTERN = Pattern.compile("^\"([^\"]+)\"[^\"]*$");
	
	private final String command;
	private final DirectoryAction action;

	public FTPCommandDirectory() {
		this(DirectoryAction.CURRENT, null);
	}
	public FTPCommandDirectory(DirectoryAction action, FTPFilename directory) {
		if (action == null) throw new NullPointerException();
		if (directory == null && action.requiresPath()) throw new IllegalArgumentException();
		
		switch (action) {
		case CURRENT: this.command = "PWD"; break;
		case CHANGE: this.command = "CWD " + directory.getPath(); break;
		case UP: this.command = "CDUP"; break;
		case REMOVE: this.command = "RMD " + directory.getPath(); break;
		case MAKE: this.command = "MKD " + directory.getPath(); break;
		default: throw new IllegalArgumentException();
		}
		
		this.action = action;
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
			int code = response.getCode();
			switch (action) {
			case CHANGE:
				if (code == 250) return FTPResult.SUCCEEDED;
				break;
			case CURRENT:
				if (code == 257) {
					String path = getResponsePath(response.getContent());
					if (path != null) {
						state.workingDirectory = new FTPFilename(state.workingDirectory, path);
						return new FTPResult(true, path.getBytes(FTPCharset.ASCII));
					}
				}
				break;
			}
		} catch (NumberFormatException e) { }
		return FTPResult.FAILED;
	}
	
	private static final String getResponsePath(String response) {
		Matcher matcher = CWD_RESPONSE_PATTERN.matcher(response);
		if (matcher.find()) return matcher.group(1);
		return null;
	}
	
	public static enum DirectoryAction {
		CHANGE(true),
		CURRENT(false),
		UP(false),
		REMOVE(true),
		MAKE(true);
		
		private final boolean requiresPath;
		private DirectoryAction(boolean requiresPath) {
			this.requiresPath = requiresPath;
		}
		public boolean requiresPath() {
			return requiresPath;
		}
	}
}
