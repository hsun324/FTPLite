package com.hsun324.ftplite;

import java.util.HashMap;
import java.util.Map;

import com.hsun324.ftplite.handlers.FTPHandlerDisconnect;

/**
 * This singleton class handles registering {@link FTPHandler}s
 * and the handling of responses by <code>FTPHandler</code>s.
 * <p>
 * All <code>FTPHandler</code>s that are to be used must be
 * registered in this class.
 * @author hsun324
 *
 */
public class FTPHandlerRegistry {
	private FTPHandlerRegistry() { }
	/**
	 * The map of handlers and their handled codes.
	 */
	private static final Map<Integer, FTPHandler> handlers = new HashMap<Integer, FTPHandler>();
	
	static {
		addHandler(new FTPHandlerDisconnect());
	}
	
	/**
	 * Adds a handler to the list of active handlers.
	 * @param handler handler to add
	 */
	public static void addHandler(FTPHandler handler) {
		int[] codes = handler.getHandledCodes();
		for (int code : codes)
			if (!handlers.containsKey(code)) handlers.put(code, handler);
	}
	/**
	 * Attempts to find a <code>FTPHandler</code> to handle
	 * the provided server response.
	 * @param state the current client state
	 * @param response the server response
	 * @return whether the response was handled
	 */
	public static boolean tryGlobalHandle(FTPState state, FTPResponse response) {
		FTPHandler handler = handlers.get(response.getCode());
		if (handler != null) return handler.pushResponse(state, response);
		return false;
	}
}
