package com.hsun324.ftplite.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Utilities {
	private Utilities() {}
	
	public static byte[] readAll(InputStream stream) throws IOException {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[100];
		int len = 0;
		while((len = stream.read(buffer)) != -1)
			outStream.write(buffer, 0, len);
		return outStream.toByteArray();
	}
	
	public static InputStream openTransferStream(boolean modeActive, String host, int port) throws IOException {
		if (modeActive) return new ServerSocket(port).accept().getInputStream();
		else return new Socket(host, port).getInputStream();
	}

	public static OutputStream openPushStream(boolean modeActive, String host, int port) throws IOException {
		if (modeActive) return new ServerSocket(port).accept().getOutputStream();
		else return new Socket(host, port).getOutputStream();
	}
}
