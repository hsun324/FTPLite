package com.hsun324.ftplite;

import java.io.IOException;


public final class FTPFuture {
	public FTPFuture(FTPClient client, FTPCommand command) {
		this.client = client;
		this.command = command;
	}
	
	protected final Object resultSync = new Object();
	
	protected final FTPClient client;
	protected final FTPCommand command;
	protected FTPResult result;
	
	public synchronized void execute() throws IOException {
		if (command.isValidContext(client.state)) {
			command.execute(client.state);
		} else result = FTPResult.FAILED;
		command.setExecuted();
	}
	
	public FTPResult getResult() throws IOException {
		waitUntilResult();
		return result;
	}
	public void waitUntilResult() throws IOException {
		synchronized (resultSync) {
			try {
				while (result == null)
					resultSync.wait();
			} catch (InterruptedException e) {
				throw new IOException(e);
			}
		}
	}
	public void setResult(FTPResult result) {
		if (result == null) return;
		synchronized (resultSync) {
			if (this.result != null) throw new IllegalStateException();
			this.result = result;
			resultSync.notifyAll();
		}
	}

	public boolean isResultSet() {
		return result != null;
	}

}
