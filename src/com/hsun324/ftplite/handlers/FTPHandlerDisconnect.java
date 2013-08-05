package com.hsun324.ftplite.handlers;

import com.hsun324.ftplite.FTPHandler;
import com.hsun324.ftplite.FTPResponse;
import com.hsun324.ftplite.FTPResult;
import com.hsun324.ftplite.FTPState;

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
