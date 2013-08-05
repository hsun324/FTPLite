package com.hsun324.ftplite.commands;

import com.hsun324.ftplite.FTPFile;
import com.hsun324.ftplite.FTPFilename;
import com.hsun324.ftplite.FTPResponse;
import com.hsun324.ftplite.FTPState;
import com.hsun324.ftplite.FTPUploadCommand;

public class FTPCommandFile extends FTPUploadCommand {
	private final String command;
	private byte[] data = null;

	public FTPCommandFile(FileAction action, FTPFilename file) {
		this(action, file, null);
	}
	public FTPCommandFile(FileAction action, FTPFilename file, FTPFile data) {
		// TODO: Current Directory / Directory Test
		if (file == null || data == null) throw new IllegalArgumentException();
		switch (action) {
		case WRITE: this.command = "STOR " + file.getPath(); break;
		case APPEND: this.command = "APPE " + file.getPath(); break;
		default: throw new IllegalArgumentException();
		}
		this.data = data.getBytes();
	}
	
	@Override
	public String getCommandContent(FTPState state) {
		return command;
	}
	@Override
	public boolean isValidContext(FTPState state) {
		return state.authCompleted && state.modeCommand != null;
	}
	
	public byte[] getData(FTPState state, FTPResponse response) {
		return data;
	}
	
	public static enum FileAction {
		WRITE,
		APPEND;
	}
}
