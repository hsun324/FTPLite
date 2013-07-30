package com.hsun324.ftplite.handlers;

import com.hsun324.ftplite.FTPResponse;
import com.hsun324.ftplite.FTPState;

public interface FTPHandler {
	public boolean handle(FTPState future, FTPResponse response);
	public int[] getHandledCodes();
}
