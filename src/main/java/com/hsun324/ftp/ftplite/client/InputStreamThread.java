package com.hsun324.ftp.ftplite.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hsun324.ftp.ftplite.FTPCharset;
import com.hsun324.ftp.ftplite.FTPHandlerRegistry;
import com.hsun324.ftp.ftplite.FTPResponse;
import com.hsun324.ftp.ftplite.FTPState;

/**
 * A <code>Thread</code> that handles the parsing and execution of
 * FTP server responses.
 * <p>
 * This <code>Thread</code> uses a <code>InputStream</code> to read
 * the server responses and direct them to the appropriate executing
 * command.
 * 
 * @author hsun324
 * @version 0.7
 */
public class InputStreamThread extends Thread {
	/**
	 * This <code>FTPStreamThread</code>'s client.
	 */
	private final AbstractFTPClient client;

	/**
	 * A <code>Pattern</code> that is used to determine whether a response is in FTP response format.
	 */
	private final Pattern CONTROL_RESPONSE_PATTERN = Pattern.compile("([0-9]{3})([ -])(.*)");

	/**
	 * The backing <code>InputStream</code>
	 */
	public InputStream inputStream;
	
	/**
	 * The wrapping <code>Reader</code> that allows for automatic conversion of bytes to chars.
	 */
	public Reader reader;
	
	/**
	 * The character buffer for input
	 */
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
	public InputStreamThread(AbstractFTPClient client) throws IOException {
		this(client, 400);
	}
	
	/**
	 * Creates a <code>FTPStreamThread</code> with the a
	 * character buffer of the specified size.
	 * @throws IOException
	 */
	public InputStreamThread(AbstractFTPClient client, int bufferSize) throws IOException {
		this.client = client;
		this.buffer = new char[bufferSize];
		this.setName("FTPL-IS-" + client.getState().host + "-" + client.getUUIDString());
	}
	
	/**
	 * Method that handles the stream logic and redirects responses to command and handlers.
	 */
	@Override
	public void run() {
		try {
			this.inputStream = client.getInputStream();
			this.reader = new InputStreamReader(this.inputStream, FTPCharset.ASCII);
			
			while (!client.isStopRequested()) {
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
							
							FTPState state = client.getState();
							
							if (!FTPHandlerRegistry.tryGlobalHandle(state, response) && state.currentFuture != null)
								if (state.currentFuture.pushResponse(response))
									state.currentFuture = null;
						} else responseBuffer.append(content).append("\n");
						continue;
					}
					responseBuffer.append(line).append("\n");
				}
			}
			reader.close();
		} catch (Exception e) {
			// TODO: Exceptions
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