package com.hsun324.ftp.ftplite.commands;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import com.hsun324.ftp.ftplite.FTPResponse;
import com.hsun324.ftp.ftplite.FTPResult;
import com.hsun324.ftp.ftplite.client.FTPInterface;

/**
 * This {@link Command} handles the data
 * connection PORT command.
 * @author hsun324
 * @version 0.7
 */
public class ModeCommandActive extends ModeCommand {
	private static final int FTP_ACTIVE_PORT_START = 1024;
	private static final int FTP_ACTIVE_PORT_END = 1024;
	
	private byte[] address = null;
	private int port = 0;
	
	@Override
	public String getCommandContent(FTPInterface inter) {
		try {
			address = InetAddress.getLocalHost().getAddress();
			port = findPort();
			return "PORT " + address[0] + "," + address[1] + "," + address[2] + "," + address[3] + "," + (port / 256) + "," + (port % 256);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return "NOOP";
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
	public FTPResult handleResponse(FTPInterface inter, FTPResponse response) {
		if (response.getCode() != 200)  return FTPResult.FAILED;
		
		inter.setDataPort(port);
		return FTPResult.SUCCEEDED;
	}

	@Override
	public boolean isActive() {
		return true;
	}
}
