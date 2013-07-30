package com.hsun324.ftplite.handlers;

import com.hsun324.ftplite.FTPResponse;
import com.hsun324.ftplite.FTPResult;
import com.hsun324.ftplite.FTPState;

public class FTPHandlerSyntax extends FTPHandler {
	private static final int[] HANDLED_CODES = new int[]{500, 501};
	
	@Override
	public FTPResult handleResponse(FTPState state, FTPResponse response) {
		return FTPResult.FAILED;
	}
	@Override
	public boolean requiresFuture() {
		return true;
	}
	@Override
	public int[] getHandledCodes() {
		return HANDLED_CODES;
	}
}
