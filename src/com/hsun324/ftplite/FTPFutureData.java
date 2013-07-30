package com.hsun324.ftplite;

import java.io.IOException;


public abstract class FTPFutureData<T> {
	public FTPFutureData(FTPFuture future) {
		this.future = future;
	}
	
	protected final FTPFuture future;
	
	public final synchronized void execute() throws IOException {
		future.execute();
	}
	public final FTPResult getResult() throws IOException {
		return future.getResult();
	}
	public final void waitUntilResult() throws IOException {
		future.waitUntilResult();
	}
	public final void setResult(FTPResult result) {
		future.setResult(result);
	}
	
	public final T getData() throws IOException {
		FTPResult result = future.getResult();
		if (!result.success) return null;
		if (result.data == null) return null;
		try {
			return formData(result.data);
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
	protected abstract T formData(byte[] result) throws Exception;
}
