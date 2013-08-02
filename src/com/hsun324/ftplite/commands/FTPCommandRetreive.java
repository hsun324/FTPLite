package com.hsun324.ftplite.commands;

import java.io.IOException;
import java.io.InputStream;

import com.hsun324.ftplite.FTPCommand;
import com.hsun324.ftplite.FTPFilename;
import com.hsun324.ftplite.FTPResponse;
import com.hsun324.ftplite.FTPResult;
import com.hsun324.ftplite.FTPState;
import com.hsun324.ftplite.FTPUtilities;

public class FTPCommandRetreive extends FTPCommand {
	private final String command;
	private byte[] data = null;
	
	public FTPCommandRetreive(FTPFilename file) {
		if (file == null || file.isCurrentDirectory()) throw new IllegalArgumentException();
		this.command = "RETR " + file.getQualifiedPath();
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
			InputStream stream = null;
			try {
				stream = FTPUtilities.openTransferStream(state.modeActive, state.dataHost, state.dataPort);
				data = FTPUtilities.readAll(stream);
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
