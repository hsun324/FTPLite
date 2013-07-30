package com.hsun324.ftplite.handlers;

import java.util.HashMap;
import java.util.Map;

import com.hsun324.ftplite.FTPResponse;
import com.hsun324.ftplite.FTPState;

public class FTPHandlerRegistry {
	private FTPHandlerRegistry() { }
	private static final Map<Integer, FTPHandler> handlers = new HashMap<Integer, FTPHandler>();
	
	static {
		addHandler(new FTPHandlerSyntax());
		addHandler(new FTPHandlerDisconnect());
	}
	
	public static void addHandler(FTPHandler handler) {
		int[] codes = handler.getHandledCodes();
		for (int code : codes)
			if (!handlers.containsKey(code)) handlers.put(code, handler);
	}
	public static boolean tryGlobalHandle(FTPState future, FTPResponse response) {
		FTPHandler handler = handlers.get(response.getCode());
		if (handler != null) return handler.handle(future, response);
		return false;
	}
}
