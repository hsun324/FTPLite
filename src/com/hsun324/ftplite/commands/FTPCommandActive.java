package com.hsun324.ftplite.commands;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import com.hsun324.ftplite.FTPCommand;
import com.hsun324.ftplite.FTPResponse;
import com.hsun324.ftplite.FTPResult;
import com.hsun324.ftplite.FTPState;

public class FTPCommandActive extends FTPCommand {
	private static final int FTP_ACTIVE_PORT_START = 1024;
	private static final int FTP_ACTIVE_PORT_END = 1024;
	
	private byte[] address = null;
	private int port = 0;

	public boolean canBeReused() {
		return true;
	}
	
	@Override
	public String getCommandContent(FTPState state) {
		try {
			address = InetAddress.getLocalHost().getAddress();
			port = findPort();
			return "PORT " + address[0] + "," + address[1] + "," + address[2] + "," + address[3] + "," + (port / 256) + "," + (port % 256);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return "NOOP";
	}
	
	@Override
	public boolean isValidContext(FTPState state) {
		return state.authCompleted;
	}
	
	private int findPort() {
		for(int i = FTP_ACTIVE_PORT_START; i <= FTP_ACTIVE_PORT_END; i++) {
			ServerSocket soc = null;
			try {
				soc = new ServerSocket(i);
				return i;
			} catch (IOException e) { } finally {
				try {
					if (soc != null) soc.close();
				} catch (IOException e) { }
			}
		}
		return 0;
	}
	
	@Override
	public FTPResult handleResponse(FTPState state, FTPResponse response) {
		if (response.getCode() != 200)  return FTPResult.FAILED;
		
		state.dataPort = port;
		return FTPResult.SUCCEEDED;
	}
}
