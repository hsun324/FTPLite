package com.hsun324.ftp.ftplite.commands;

import com.hsun324.ftp.ftplite.FTPFile;
import com.hsun324.ftp.ftplite.FTPObject;
import com.hsun324.ftp.ftplite.FTPResponse;
import com.hsun324.ftp.ftplite.client.FTPInterface;
import com.hsun324.ftp.ftplite.client.FTPInterface.ClientState;

/**
 * This {@link Command} handles file data
 * commands like STOR and APPE.
 * @author hsun324
 * @version 0.7
 */
public class UploadCommandFile extends UploadCommand {
	private final String command;
	private byte[] data = null;

	public UploadCommandFile(FileAction action, FTPObject file) {
		this(action, file, null);
	}
	public UploadCommandFile(FileAction action, FTPObject file, FTPFile data) {
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
	public String getCommandContent(FTPInterface inter) {
		return command;
	}
	@Override
	public boolean isValidContext(FTPInterface inter) {
		return inter.getClientState() == ClientState.READY;
	}
	
	public byte[] getData(FTPInterface inter, FTPResponse response) {
		return data;
	}
	
	public static enum FileAction {
		WRITE,
		APPEND;
	}
}
