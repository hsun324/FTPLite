package com.hsun324.ftp.ftplite.commands;

import com.hsun324.ftp.ftplite.FTPResponse;
import com.hsun324.ftp.ftplite.FTPResult;
import com.hsun324.ftp.ftplite.FTPFile.FTPFiletype;
import com.hsun324.ftp.ftplite.client.FTPInterface;
import com.hsun324.ftp.ftplite.client.FTPInterface.ClientState;

/**
 * This {@link Command} handles the transfer
 * type TYPE command.
 * @author hsun324
 * @version 0.7
 */
public class TextCommandType extends TextCommand {
	public static boolean typeSupported(char type) {
		return type == 'A' || type == 'I';
	}
	
	private final char type;
	private final String command;
	
	public TextCommandType(char typeChar) {
		if (!typeSupported(typeChar)) throw new IllegalArgumentException();
		this.type = typeChar;
		this.command = "TYPE " + typeChar;
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
		if (response.getCode() != 200) return FTPResult.FAILED;
		inter.setCurrentFiletype(type == 'I' ? FTPFiletype.BINARY : FTPFiletype.ASCII);
		return FTPResult.SUCCEEDED;
	}
}
