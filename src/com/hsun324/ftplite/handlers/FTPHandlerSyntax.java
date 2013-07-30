package com.hsun324.ftplite.handlers;

import com.hsun324.ftplite.FTPResponse;
import com.hsun324.ftplite.FTPResult;
import com.hsun324.ftplite.FTPState;

public class FTPHandlerSyntax implements FTPHandler {
	@Override
	public boolean handle(FTPState state, FTPResponse response) {
		state.currentFuture.setResult(FTPResult.FAILED);
		return true;
	}
	private static final int[] HANDLED = new int[]{500, 501};
	@Override
	public int[] getHandledCodes() {
		return HANDLED;
	}
}
