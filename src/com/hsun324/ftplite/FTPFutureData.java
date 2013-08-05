package com.hsun324.ftplite;

import java.io.IOException;

/**
 * This class wraps a {@link FTPFuture} and provides a abstraction upon
 * data transformation into certain types of information by using a
 * <code>formData(byte[])</code> method.
 * <p>
 * This class is synchronized across threads by means of <code>FTPFuture</code>
 * synchronization. Methods that retrieve the result will block until the result
 * has been set.
 * @author hsun324
 *
 * @param <T>
 * @version 0.5
 * @since 0.5
 */
public abstract class FTPFutureData<T> {
	/**
	 * Creates a <code>FTPFutureData</code> from the provided <code>FTPFuture</code>.
	 * @param future the wrapped future
	 */
	public FTPFutureData(FTPFuture future) {
		this.future = future;
	}
	
	/**
	 * The backing <code>FTPFuture</code>.
	 */
	protected final FTPFuture future;
	
	/**
	 * Calls the <code>execute()</code> method on the backing future.
	 * @throws IOException
	 */
	public final synchronized void execute() throws IOException {
		future.execute();
	}
	/**
	 * Calls the <code>getResult()</code> method on the backing future.
	 * @return the result
	 * @throws IOException
	 */
	public final FTPResult getResult() throws IOException {
		return future.getResult();
	}
	
	/**
	 * Calls the <code>waitUntilResult()</code> method on the backing future.
	 * @throws IOException
	 */
	public final void waitUntilResult() throws IOException {
		future.waitUntilResult();
	}

	/**
	 * Calls the <code>setResult(FTPResult)</code> method on the backing future.
	 * @throws IOException
	 */
	public final void setResult(FTPResult result) {
		future.setResult(result);
	}

	/**
	 * Flag indicating whether the data is set.
	 */
	protected boolean set = false;
	
	/**
	 * Processed data cache field.
	 */
	protected T data = null;
	/**
	 * Gets the data from the result by transforming it using the
	 * <code>formData(byte[])</code> method.
	 * <p>
	 * If the result is not yet set, this method will block until
	 * the result is set.
	 * @return the data
	 * @throws IOException
	 */
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
	
	/**
	 * Forms the data provided by the byte array into the structure
	 * provided by the <code>T</code> generic type.
	 * <p>
	 * Subclasses of <code>FTPFutureData</code> should implement this method.
	 * @param result the data
	 * @return the formed structure
	 * @throws Exception
	 */
	protected abstract T formData(byte[] result) throws Exception;
}
