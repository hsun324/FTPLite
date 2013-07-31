package com.hsun324.ftplite.commands;

import java.io.IOException;
import java.io.OutputStream;

import com.hsun324.ftplite.FTPCommand;
import com.hsun324.ftplite.FTPResponse;
import com.hsun324.ftplite.FTPResult;
import com.hsun324.ftplite.FTPState;
import com.hsun324.ftplite.FTPUtilities;

public class FTPCommandPut extends FTPCommand {
	private final String command;
	private byte[] data = null;
	
	public FTPCommandPut(String file, String data) {
		this.command = "STOR " + file;
		this.data = data.getBytes();
	}
	
	@Override
	public String getCommandContent(FTPState state) {
		return command;
	}
	@Override
	public boolean isValidContext(FTPState state) {
		return state.authCompleted && state.modeCommand != null;
	}
	@Override
	public FTPResult handleResponse(FTPState state, FTPResponse response) {
		if (response.getCode() == 150 || response.getCode() == 125) {
			OutputStream stream = null;
			try {
				stream = FTPUtilities.openPushStream(state.modeActive, state.dataHost, state.dataPort);
				stream.write(data);
				stream.flush();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (stream != null)
						stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else if (response.getCode() == 226 && data != null) return new FTPResult(true, data);
		
		return FTPResult.FAILED;
	}
}
