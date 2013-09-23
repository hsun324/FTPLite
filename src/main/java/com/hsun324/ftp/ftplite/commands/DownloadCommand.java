package com.hsun324.ftp.ftplite.commands;

import java.io.IOException;
import java.io.InputStream;

import com.hsun324.ftp.ftplite.FTPResponse;
import com.hsun324.ftp.ftplite.FTPResult;
import com.hsun324.ftp.ftplite.FTPUtilities;
import com.hsun324.ftp.ftplite.client.FTPInterface;

/**
 * This class represents a command that requires a download from
 * the data channel for proper execution.
 * @author hsun324
 * @version 0.7
 */
public abstract class DownloadCommand extends TextCommand {
	/**
	 * Download stream semaphore.
	 */
	private Object downloadSync = new Object();
	
	/**
	 * The <code>InputStream</code> for downloading data.
	 */
	private InputStream download = null;
	
	/**
	 * The data downloaded from the download stream.
	 */
	private byte[] data = null;
	
	/**
	 * Flag indicating whether a stop has been requested.
	 */
	private boolean stopRequested = false;
	
	/**
	 * Gets the download stream.
	 * @return the download stream
	 */
	public final InputStream getDownload() {
		return download;
	}
	
	public void execute(FTPInterface inter) throws IOException {
		// TODO: Async
		super.execute(inter);
		synchronized (downloadSync) {
			download = FTPUtilities.openTransferStream(inter.getModeCommand(), inter.getDataHost(), inter.getDataPort());
			downloadSync.notify();
		}
	}
	
	@Override
	public void quitExecution() {
		synchronized (downloadSync) {
			stopRequested = true;
			downloadSync.notifyAll();
		}
	}
	
	@Override
	public final FTPResult handleResponse(FTPInterface inter, FTPResponse response) {
		if (response.getCode() == 150 || response.getCode() == 125) {
			try {
				synchronized (downloadSync) {
					while (download == null && !stopRequested) downloadSync.wait();
					if (download != null) data = processData(inter, response, FTPUtilities.readAll(download));
				}
				return null;
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				try {
					if (download != null)
						download.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else if (response.getCode() == 226 && data != null) return new FTPResult(true, data);
		return FTPResult.FAILED;
	}
	
	/**
	 * Processes the data downloaded from the download stream.
	 * <p>
	 * Subclasses of <code>FTPDownloadCommand</code> should implement this method.
	 * @param state the current client state
	 * @param response the server response
	 * @param data the downloaded data
	 * @return the data for the result
	 */
	public abstract byte[] processData(FTPInterface inter, FTPResponse response, byte[] data);
}
