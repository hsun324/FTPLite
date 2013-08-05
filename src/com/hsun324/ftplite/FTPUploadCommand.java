package com.hsun324.ftplite;

import java.io.IOException;
import java.io.OutputStream;

public abstract class FTPUploadCommand extends FTPCommand {
	private Object uploadSync = new Object();
	private OutputStream upload = null;
	private byte[] data = null;

	private boolean stopRequested = false;
	
	public final OutputStream getUpload() {
		return upload;
	}
	public void execute(FTPState state) throws IOException {
		// TODO: Async
		super.execute(state);
		synchronized (uploadSync) {
			upload = FTPUtilities.openPushStream(state.modeActive, state.dataHost, state.dataPort);
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
	public final FTPResult handleResponse(FTPState state, FTPResponse response) {
		if (response.getCode() == 150 || response.getCode() == 125) {
			try {
				synchronized (uploadSync) {
					while (upload == null && !stopRequested) uploadSync.wait();
					upload.write(getData(state, response));
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
		} else if (response.getCode() == 226 && data != null) return FTPResult.SUCCEEDED;
		return FTPResult.FAILED;
	}
	public abstract byte[] getData(FTPState state, FTPResponse response);
}
