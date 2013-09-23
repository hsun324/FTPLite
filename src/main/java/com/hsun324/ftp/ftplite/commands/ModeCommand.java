package com.hsun324.ftp.ftplite.commands;

import com.hsun324.ftp.ftplite.client.FTPInterface;
import com.hsun324.ftp.ftplite.client.FTPInterface.ClientState;

/**
 * This class represents a command that requires a download from
 * the data channel for proper execution.
 * @author hsun324
 * @version 0.7
 */
public abstract class ModeCommand extends TextCommand {
	@Override
	public boolean isValidContext(FTPInterface inter) {
		return inter.getClientState() == ClientState.READY;
	}
	
	public abstract boolean isActive();
}
