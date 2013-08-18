package com.hsun324.ftp.ftplite;

/**
 * This abstract class represents a global response code
 * handler that takes precedence over the base command
 * handlers.
 * <p>
 * <code>FTPHandler</code>s must be registered into the 
 * {@link FTPHandlerRegistry} using the
 * <code>addHandler(FTPHandler)</code> method.
 * @author hsun324
 * @version 0.6a
 */
public abstract class FTPHandler {
	/**
	 * Attempts to call a handler from the {@link FTPHandlerRegistry}.
	 * <p>
	 * If the handler successfully handled the response, this method
	 * will return true.
	 * @param state the current client state
	 * @param response the server response
	 * @return whether the response was handled
	 */
	public final boolean pushResponse(FTPState state, FTPResponse response) {
		FTPFuture future = state.currentFuture;
		if (requiresFuture() && future == null) return false;
		
		FTPResult result = handleResponse(state, response);
		if (result != null) {
			if (future != null) future.setResult(result);
			return true;
		}
		return false;
	}
	
	/**
	 * Gets the codes handled by this <code>FTPHandler</code>.
	 * <p>
	 * Subclasses of <code>FTPHandler</code> should implement this method
	 * to return a fixed response.
	 * @return codes handled
	 */
	public abstract int[] getHandledCodes();
	/**
	 * Causes this handler to attempt to handle the provided response.
	 * <p>
	 * If this method returns a <code>FTPResult</code> that is not null, it
	 * will be interpreted as being a successful handle.
	 * <p>
	 * Subclasses of <code>FTPHandler</code> should implement this method.
	 * @param state the current client state
	 * @param response the server response
	 * @return the result of the handling
	 */
	public abstract FTPResult handleResponse(FTPState state, FTPResponse response);
	/**
	 * Gets whether this <code>FTPHandler</code> requires the <code>currentFuture</code>
	 * in the current client state to be set.
	 * <p>
	 * Subclasses of <code>FTPHandler</code> should implement this method.
	 * @return whether this requires future or not
	 */
	public abstract boolean requiresFuture();
}
