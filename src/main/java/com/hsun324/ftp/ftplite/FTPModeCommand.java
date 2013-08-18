package com.hsun324.ftp.ftplite;

/**
 * This class represents a command that requires a download from
 * the data channel for proper execution.
 * @author hsun324
 * @version 0.7
 */
public abstract class FTPModeCommand extends FTPCommand {
	
	@Override
	public boolean canBeReused() {
		return true;
	}
	
	@Override
	public boolean isValidContext(FTPState state) {
		return state.authCompleted;
	}
	
	public abstract boolean isActive();
}
