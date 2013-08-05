package com.hsun324.ftplite;

import java.io.IOException;
import java.io.InputStream;

public abstract class FTPDownloadCommand extends FTPCommand {
	private Object downloadSync = new Object();
	private InputStream download = null;
	private byte[] data = null;
	
	private boolean stopRequested = false;
	
	public final InputStream getDownload() {
		return download;
	}
	public void execute(FTPState state) throws IOException {
		// TODO: Async
		super.execute(state);
		synchronized (downloadSync) {
			download = FTPUtilities.openTransferStream(state.modeActive, state.dataHost, state.dataPort);
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
	public final FTPResult handleResponse(FTPState state, FTPResponse response) {
		if (response.getCode() == 150 || response.getCode() == 125) {
			try {
				synchronized (downloadSync) {
					while (download == null && !stopRequested) downloadSync.wait();
					if (download != null) data = processData(state, response, FTPUtilities.readAll(download));
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
	public abstract byte[] processData(FTPState state, FTPResponse response, byte[] data);
}
