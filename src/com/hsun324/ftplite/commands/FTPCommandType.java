package com.hsun324.ftplite.commands;

import com.hsun324.ftplite.FTPCommand;
import com.hsun324.ftplite.FTPResponse;
import com.hsun324.ftplite.FTPResult;
import com.hsun324.ftplite.FTPState;

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

	public boolean canBeReused() {
		return true;
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
