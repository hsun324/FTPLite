package com.hsun324.ftp.ftplite.handlers;

import com.hsun324.ftp.ftplite.FTPHandler;
import com.hsun324.ftp.ftplite.FTPResponse;
import com.hsun324.ftp.ftplite.FTPResult;
import com.hsun324.ftp.ftplite.FTPState;

/**
 * This {@link FTPHandler} handles the general server-initiated
 * disconnect code, 421.
 * @author hsun324
 * @version 0.7
 */
public class FTPHandlerDisconnect extends FTPHandler {
	private static final int[] HANDLED_CODES = new int[]{421};
	
	@Override
	public boolean requiresFuture() {
		return false;
	}
	@Override
	public FTPResult handleResponse(FTPState state, FTPResponse response) {
		state.client.close();
		return FTPResult.FAILED;
	}
	@Override
	public int[] getHandledCodes() {
		return HANDLED_CODES;
	}
}
