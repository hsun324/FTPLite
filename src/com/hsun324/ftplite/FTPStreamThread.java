package com.hsun324.ftplite;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hsun324.ftplite.handlers.FTPHandlerRegistry;

class FTPStreamThread extends Thread {
	public static final int DEFAULT_BUFFER_SIZE = 400;
	private static final Pattern CONTROL_RESPONSE_PATTERN = Pattern.compile("([0-9]{3})([ -])(.*)");

	private final FTPClient client;
	public final Socket socket;
	public final InputStream inputStream;
	public final Reader reader;
	
	private final char[] buffer;
	private boolean stopRequested = false;
	
	private StringBuffer responseBuffer = new StringBuffer();
	
	public FTPStreamThread(FTPClient client, Socket socket) throws IOException {
		this(client, socket, DEFAULT_BUFFER_SIZE);
	}
	public FTPStreamThread(FTPClient client, Socket socket, int bufferSize) throws IOException {
		this.client = client;
		this.socket = socket;
		this.inputStream = socket.getInputStream();
		this.reader = new InputStreamReader(this.inputStream);
		this.buffer = new char[bufferSize];
	}
	
	@Override
	public void run() {
		try {
			while (!stopRequested) {
				int read = reader.read(buffer);
				int newline;
				while ((newline = findNewlineIndex(buffer, read)) > -1) {
					String line = new String(buffer, 0, newline);
					
					int shift = newline + 2;
					int len = buffer.length;
					int threshold = len - shift;
					for(int i = 0; i < len; i++)
						if (i < threshold) buffer[i] = buffer[i + shift];
						else buffer[i] = 0;
					
					System.out.println("  " + line);
					
					Matcher matcher = CONTROL_RESPONSE_PATTERN.matcher(line);
					if (matcher.find()) {
						int code = Integer.parseInt(matcher.group(1));
						String delim = matcher.group(2);
						String content = matcher.group(3);
						
						if (delim.equals(" ")) {
							FTPResponse response = new FTPResponse(code, responseBuffer.append(content).toString());
							responseBuffer.setLength(0);
							
							if (!FTPHandlerRegistry.tryGlobalHandle(client.state, response) &&
								client.state.currentFuture.command.pushResponse(client.state, response))
									client.state.currentFuture = null;
							continue;
						}
					}
					responseBuffer.append(line).append("\n");
				}
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void requestStop() {
		stopRequested = true;
	}
	private int findNewlineIndex(char[] buffer, int len) {
		for (int i = 0, j = 1; j < len; i++, j++)
			if (buffer[i] == '\r' && buffer[j] == '\n') return i;
		return -1;
	}
}