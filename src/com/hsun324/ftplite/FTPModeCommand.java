package com.hsun324.ftplite;

/**
 * This class represents a command that requires a download from
 * the data channel for proper execution.
 * @author hsun324
 * @version 0.6a
 * @since 0.6a
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
