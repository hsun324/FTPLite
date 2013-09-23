package com.hsun324.ftp.ftplite.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hsun324.ftp.ftplite.FTPFile.FTPFiletype;
import com.hsun324.ftp.ftplite.FTPObject;
import com.hsun324.ftp.ftplite.FTPResponse;
import com.hsun324.ftp.ftplite.FTPResult;
import com.hsun324.ftp.ftplite.FTPObject.FTPObjectType;
import com.hsun324.ftp.ftplite.client.FTPInterface;
import com.hsun324.ftp.ftplite.client.FTPInterface.ClientState;

/**
 * This {@link Command} handles directory functions
 * like PWD, CWD, CDUP, RMD, and MKD.
 * @author hsun324
 * @version 0.7
 */
public class TextCommandDirectory extends TextCommand {
	private static final Pattern CWD_RESPONSE_PATTERN = Pattern.compile("^\"([^\"]+)\"[^\"]*$");
	
	private final String command;
	private final DirectoryAction action;

	public TextCommandDirectory() {
		this(DirectoryAction.CURRENT, null);
	}
	public TextCommandDirectory(DirectoryAction action, FTPObject directory) {
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
	public String getCommandContent(FTPInterface inter) {
		return command;
	}
	@Override
	public boolean isValidContext(FTPInterface inter) {
		return inter.getClientState() == ClientState.READY;
	}
	
	@Override
	public FTPResult handleResponse(FTPInterface inter, FTPResponse response) {
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
						inter.setCurrentDirectory(new FTPObject(FTPObjectType.DIRECTORY, inter.getCurrentDirectory(), path));
						return new FTPResult(true, path.getBytes(FTPFiletype.ASCII.getCharset()));
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
