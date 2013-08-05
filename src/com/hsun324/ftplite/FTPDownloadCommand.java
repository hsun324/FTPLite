package com.hsun324.ftplite;

import java.io.IOException;
import java.io.InputStream;

public abstract class FTPDownloadCommand extends FTPCommand {
	private InputStream download = null;
	private byte[] data = null;
	
	public final InputStream getDownload() {
		return download;
	}
	public void execute(FTPState state) throws IOException {
		// TODO: Async
		super.execute(state);
		download = FTPUtilities.openTransferStream(state.modeActive, state.dataHost, state.dataPort);
	}
	@Override
	public final FTPResult handleResponse(FTPState state, FTPResponse response) {
		if (response.getCode() == 150 || response.getCode() == 125) {
			try {
				data = processData(state, response, FTPUtilities.readAll(download));
				return null;
			} catch (IOException e) {
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
