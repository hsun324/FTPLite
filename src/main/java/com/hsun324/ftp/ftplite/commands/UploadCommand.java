package com.hsun324.ftp.ftplite.commands;

import java.io.IOException;
import java.io.OutputStream;

import com.hsun324.ftp.ftplite.FTPResponse;
import com.hsun324.ftp.ftplite.FTPResult;
import com.hsun324.ftp.ftplite.FTPUtilities;
import com.hsun324.ftp.ftplite.client.FTPInterface;


/**
 * This class represents a command that requires a upload to
 * the data channel for proper execution.
 * @author hsun324
 * @version 0.7
 */
public abstract class UploadCommand extends TextCommand {
	/**
	 * Upload stream semaphore.
	 */
	private Object uploadSync = new Object();
	
	/**
	 * The <code>OutputStream</code> for downloading data.
	 */
	private OutputStream upload = null;
	
	/**
	 * Flag indicating whether a stop has been requested.
	 */
	private boolean stopRequested = false;

	/**
	 * Gets the upload stream.
	 * @return the upload stream
	 */
	public final OutputStream getUpload() {
		return upload;
	}
	public void execute(FTPInterface inter) throws IOException {
		// TODO: Async
		synchronized (uploadSync) {
			upload = FTPUtilities.openPushStream(inter.getModeCommand(), inter.getDataHost(), inter.getDataPort());
			uploadSync.notify();
		}
	}
	
	@Override
	public void quitExecution() {
		synchronized (uploadSync) {
			stopRequested = true;
			uploadSync.notifyAll();
		}
	}
	
	@Override
	public final FTPResult handleResponse(FTPInterface inter, FTPResponse response) {
		if (response.getCode() == 150 || response.getCode() == 125) {
			try {
				synchronized (uploadSync) {
					while (upload == null && !stopRequested) uploadSync.wait();
					upload.write(getData(inter, response));
					upload.flush();
				}
				return null;
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				try {
					if (upload != null)
						upload.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else if (response.getCode() == 226) return FTPResult.SUCCEEDED;
		return FTPResult.FAILED;
	}
	
	/**
	 * Gets the data that should be uploaded to the upload stream.
	 * <p>
	 * Subclasses of <code>FTPUploadCommand</code> should implement this method.
	 * @param state the current client state
	 * @param response the server response
	 * @return the data to send
	 */
	public abstract byte[] getData(FTPInterface inter, FTPResponse response);
}
