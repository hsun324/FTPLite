package com.hsun324.ftp.ftplite.commands;

import com.hsun324.ftp.ftplite.FTPCommand;
import com.hsun324.ftp.ftplite.FTPResponse;
import com.hsun324.ftp.ftplite.FTPResult;
import com.hsun324.ftp.ftplite.FTPState;

/**
 * This {@link FTPCommand} handles the transfer
 * type TYPE command.
 * @author hsun324
 * @version 0.6a
 * @since 0.5
 */
public class FTPCommandType extends FTPCommand {
	public static boolean typeSupported(char type) {
		return type == 'A' || type == 'I';
	}
	
	private final char type;
	private final String command;
	
	public FTPCommandType(char typeChar) {
		if (!typeSupported(typeChar)) throw new IllegalArgumentException();
		this.type = typeChar;
		this.command = "TYPE " + typeChar;
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
		if (response.getCode() != 200) return FTPResult.FAILED;
		state.typeImage = type == 'I';
		return FTPResult.SUCCEEDED;
	}
}
