package com.hsun324.ftplite.handlers;

import com.hsun324.ftplite.FTPResponse;
import com.hsun324.ftplite.FTPResult;
import com.hsun324.ftplite.FTPState;

public abstract class FTPHandler {
	public final boolean pushResponse(FTPState state, FTPResponse response) {
		if (requiresFuture() && state.currentFuture == null) return false;
		
		FTPResult result = handleResponse(state, response);
		if (result != null) {
			if (state.currentFuture != null) state.currentFuture.setResult(result);
			return true;
		}
		return false;
	}
	
	public abstract int[] getHandledCodes();
	public abstract FTPResult handleResponse(FTPState state, FTPResponse response);
	public abstract boolean requiresFuture();
}
