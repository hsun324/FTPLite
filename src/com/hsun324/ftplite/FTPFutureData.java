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

	protected boolean set = false;
	protected T data = null;
	public final T getData() throws IOException {
		if (set) return data;
		
		T ret = null;
		FTPResult result = future.getResult();
		if (result.success && result.data != null) {
			try {
				ret = formData(result.data);
			} catch (IOException e) {
				throw e;
			} catch (Exception e) {
				throw new IOException(e);
			}
		}
		set = true;
		data = ret;
		return data;
	}
	protected abstract T formData(byte[] result) throws Exception;
}
