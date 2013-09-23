package com.hsun324.ftp.ftplite.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hsun324.ftp.ftplite.FTPFuture;
import com.hsun324.ftp.ftplite.FTPResponse;
import com.hsun324.ftp.ftplite.FTPFile.FTPFiletype;

/**
 * A <code>Thread</code> that handles the execution of queued
 * commands.
 * @author hsun324
 * @version 0.7
 */
class FTPClientThread extends Thread {
	/**
	 * A <code>Pattern</code> that is used to determine whether a response is in FTP response format.
	 */
	private static final Pattern CONTROL_RESPONSE_PATTERN = Pattern.compile("([0-9]{3})([ -])(.*)");

	
	private final FTPClient client;
	private final FTPInterface inter;
	
	private final List<FTPFuture> queue;
	
	private InputStream inputStream = null;
	private Reader reader = null;
	
	private final char[] buffer;
	
	/**
	 * A buffer that holds previous lines in multiple line responses.
	 */
	private StringBuffer responseBuffer = new StringBuffer();
	
	/**
	 * Creates a <code>FTPStreamThread</code> with the standard size
	 * character buffer.
	 * @param client The client this thread runs for
	 * @throws IOException
	 */
	public FTPClientThread(FTPClient client) {
		this(client, 400);
	}

	/**
	 * Creates a <code>FTPClientThread</code>.
	 * @param client the client this thread runs for
	 */
	protected FTPClientThread(FTPClient client, int bufferSize) {
		this.client = client;
		this.inter = client.getInterface();
		this.queue = inter.getCommandQueue();
		this.buffer = new char[bufferSize];
		this.setName("FTPL_THREAD_" + inter.getUniqueString());
	}
	
	/**
	 * Method that handles the queue logic and executes commands in order.
	 */
	@Override
	public void run() {
		try {
			this.inputStream = client.getInputStream();
			this.reader = new InputStreamReader(this.inputStream, FTPFiletype.ASCII.getCharset());

			while (!inter.isClosing()) {
				try {
					synchronized (this.queue) {
						while (this.queue.size() == 0) {
							this.queue.wait();
						}
					}
					
					FTPFuture future = this.queue.remove(0);
					inter.setFuture(future);
					future.execute();

					while (!inter.isClosing() && !future.completed()) {
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
									
									if (future.pushResponse(response)) break;
								} else responseBuffer.append(content).append("\n");
								continue;
							}
							responseBuffer.append(line).append("\n");
						}
					}
				} catch (IOException e) {
					// TODO: Exceptions
				}
			}
		} catch (InterruptedException e) {
			// TODO: Exceptions
		} catch (IOException e) {
			// TODO: Exceptions
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// TODO: Exceptions
				}
			}
		}
	}
	
	/**
	 * Gets the index of the first FTP newline (<code>\r\n</code>) in the character
	 * array within <code>length</code> characters or the start.
	 * @param array the array to look in
	 * @param length the length to loop up to
	 */
	private int findNewlineIndex(char[] array, int length) {
		for (int i = 0, j = 1; j < length; i++, j++)
			if (array[i] == '\r' && array[j] == '\n') return i;
		return -1;
	}
}