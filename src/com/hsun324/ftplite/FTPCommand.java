package com.hsun324.ftplite;

import java.io.IOException;

public abstract class FTPCommand {
	protected final Object executionSync = new Object();
	private boolean executed = false;
	public final void setExecuted() {
		executed = true;
	}
	public final boolean isExecuted() {
		return executed;
	}

	public void execute(FTPState state) throws IOException {
		if (isExecuted()) throw new IllegalStateException();
		System.out.println("> " + getCommandContent());
		state.client.getCommandStream().write(new StringBuilder(getCommandContent()).append("\r\n").toString().getBytes());
	}
	public String getCommandContent() {
		return "NOOP";
	}
	
	public boolean isValidContext(FTPState state) {
		return true;
	}
	public final boolean pushResponse(FTPState state, FTPResponse response) {
		synchronized (executionSync) {
			FTPResult result = handleResponse(state, response);
			if (result != null) {
				state.currentFuture.setResult(result);
				return true;
			}
			return false;
		}
	}
	public abstract FTPResult handleResponse(FTPState state, FTPResponse response);
}
