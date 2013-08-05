package com.hsun324.ftplite;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hsun324.ftplite.commands.*;
import com.hsun324.ftplite.commands.FTPCommandDirectory.DirectoryAction;
import com.hsun324.ftplite.commands.FTPCommandFile.FileAction;

/**
 * Represents a typical File Transfer Protocol (FTP) client
 * as defined by RFC 959. This class provides methods to manipulate
 * files and folders on remote systems using FTP.
 * <p>
 * The <code>FTPClient</code> is synchronized over multiple threads. Commands sent
 * to the server are not executed immediately, but instead are
 * represented by either {@link FTPFuture} or {@link FTPFutureData} objects with
 * blocking result retrieval methods.
 * 
 * @author hsun324
 * @version 0.6a
 */
public class FTPClient {
	// TODO: Exception handling everywhere
	
	/**
	 * Default FTP connection port
	 * @since 0.5
	 */
	public static final int DEFAULT_FTP_SERVER_PORT = 21;
	
	// TODO: Synchronization
	/**
	 * Thread name ticker
	 */
	private static int THREAD_NAME_TICKER = 0;
	
	/**
	 * Creates a <code>FTPClient</code> bound to the provided host using the default FTP port.
	 * @param host address to connect to
	 * @since 0.5
	 */
	public FTPClient(String host) {
		this(host, DEFAULT_FTP_SERVER_PORT);
	}
	
	/**
	 * Creates a <code>FTPClient</code> bound to the provided host using the provided port.
	 * @param host address to connect to
	 * @param port port to connect to
	 * @since 0.5
	 */
	public FTPClient(String host, int port) {
		super();
		this.state = new FTPState(this, host, port);
	}
	
	/**
	 * The state of the current <code>FTPClient</code>.
	 * <p>
	 * This state should never be leaked out of this library from a method within this class.
	 * @since 0.5
	 */
	protected final FTPState state;
	
	/**
	 * The queue of commands waiting to be executed.
	 * <p>
	 * This <code>List</code> is not inherently synchronized, therefore all accesses to this
	 * <code>List</code> should be synchronized externally.
	 * @since 0.5
	 */
	protected final List<FTPFuture> commandQueue = new ArrayList<FTPFuture>();
	
	/**
	 * Connection access semaphore
	 * @since 0.5
	 */
	private final Object connectionSync = new Object();
	
	/**
	 * The underlying <code>Socket</code> used to handle the control connection for two-way
	 * communication with the server.
	 */
	private Socket connection = null;
	
	/**
	 * The {@link FTPStreamThread} that handles the receiving of command responses from the server.
	 * @since 0.5
	 */
	private FTPStreamThread responseThread = null;
	
	/**
	 * The <code>OutputStream</code> that is connected to the server used to send commands.
	 */
	private OutputStream commandStream = null;

	/**
	 * The {@link FTPQueueThread} that handles the retrieval and execution of queued commands.
	 * @since 0.5
	 */
	private FTPQueueThread queueThread = null;

	/**
	 * Gets the <code>OutputStream</code> that is used to send commands to the connected server.
	 * @return the control line output stream
	 */
	protected OutputStream getCommandStream() {
		return commandStream;
	}
	
	/**
	 * Adds a command to the command queue for later execution and returns an {@link FTPFuture} element representing
	 * the progress of the execution of the current command.
	 * @param command the command to queue
	 * @return a ftp future representing the command
	 * @throws IOException
	 * @since 0.5
	 */
	public FTPFuture queueCommand(FTPCommand command) throws IOException {
		synchronized (commandQueue) {
			if (!state.connected)  throw new IllegalStateException("conn closed");
			return preQueueCommand(command);
		}
	}
	
	/**
	 * Adds a command to the command queue for later execution and returns an {@link FTPFuture} element representing
	 * the progress of the execution of the current command.
	 * <p>
	 * This method does not check the status of the connection or queue thread before adding the command to the
	 * queue.
	 * @param command the command to queue
	 * @return a ftp future representing the command
	 * @throws IOException
	 * @since 0.5
	 */
	private FTPFuture preQueueCommand(FTPCommand command) throws IOException {
		synchronized (commandQueue) {
			if (command.isExecuted()) throw new IllegalStateException("already executed command");
			
			FTPFuture future = new FTPFuture(this, command);
			commandQueue.add(future);
			commandQueue.notify();
			return future;
		}
	}
	
	/**
	 * If not yet connected to the server, this method causes this <code>FTPClient</code> connect
	 * to the server using the provided user and password. If already connected, this method will
	 * issue a reinitialize command and login using the provided user and password.
	 * <p>
	 * The <code>FTPClient</code> may not immediately connect to server and be ready for commands. This
	 * method returns a {@link FTPFuture} that will recieve a result when the client is connected and
	 * ready.
	 * <p>
	 * If the password is <code>null</code> then this method will assume that the user requires no
	 * password and will cause the connection to fail if one is required.
	 * @param user the user to connect as
	 * @param password the password to use
	 * @return a ftp future representing the connection state
	 * @throws IOException
	 */
	public synchronized FTPFuture connect(String user, String password) throws IOException {
		try {
			FTPCommand firstCommand = null;
			
			if (!state.connected) {
				queueThread = new FTPQueueThread();
				queueThread.start();
				
				responseThread = new FTPStreamThread();
				responseThread.start();
				
				THREAD_NAME_TICKER++;
				
				firstCommand = new FTPCommandConnect();
			} else firstCommand = new FTPCommandReinitialize();
			
			FTPFuture future = preQueueCommand(new FTPCommandChained(new FTPCommand[]{
				firstCommand,
				new FTPCommandUser(user),
				new FTPCommandPassword(password),
				new FTPCommandSystem(),
				new FTPCommandFeatures(),
				new FTPCommandDirectory(),
				new FTPCommandUTF8()
			}));
			
			if (!state.connected) {
				putConnection(new Socket(state.host, state.port));
			}
			
			return future;
		} catch (Exception e) {
			close();
			throw new IOException("could not make connection", e);
		}
	}

	/**
	 * A synchronization helper method that sets the connection and notifies
	 * all threads waiting for a connection object that one is available.
	 * @param connection the connection to use
	 * @throws IOException
	 */
	private void putConnection(Socket connection) throws IOException {
		synchronized (connectionSync) {
			this.connection = connection;
			this.commandStream = connection.getOutputStream();
			connectionSync.notifyAll();
		}
	}
	
	/**
	 * Queues a command for the <code>FTPClient</code> to disconnect.
	 * @throws IOException
	 */
	public void quit() throws IOException {
		queueCommand(new FTPCommandQuit());
	}
	
	/**
	 * Closes the current connection and frees related resources.
	 * @throws IOException
	 */
	public synchronized void close() {
		try {
			if (responseThread != null) {
				responseThread.requestStop();
				responseThread = null;
			}

			if (queueThread != null) {
				queueThread.requestStop();
				queueThread = null;
			}
			
			if (commandStream != null) {
				commandStream.close();
				commandStream = null;
			}

			if (connection != null) {
				connection.close();
				connection = null;
			}
			
			FTPFuture future = state.currentFuture;
			if (future != null) {
				future.command.quitExecution();
				if (!future.isResultSet()) future.setResult(FTPResult.FAILED);
			}
			
			state.reset();
			state.connected = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets the current transmission stream mode to active which
	 * means that the server must start the data connection.
	 * @throws IOException
	 * @since 0.5
	 */
	public void setActiveMode() throws IOException {
		state.modeActive = true;
		state.modeCommand = new FTPCommandActive();
	}
	
	/**
	 * Sets the current transmission stream mode to passive which
	 * means that the client must start the data connection.
	 * @throws IOException
	 * @since 0.5
	 */
	public void setPassiveMode() throws IOException {
		state.modeActive = false;
		state.modeCommand = new FTPCommandPassive();
	}

	/**
	 * Queues a data command to retrieve a file denoted by the file.
	 * <p>
	 * This method returns a {@link FTPFutureData} object of the
	 * generic {@link FTPFile} which represents the command and the
	 * data contained in the file being retrieved.
	 * @param file the file to retreive
	 * @return a ftp future representing the command
	 * @throws IOException
	 * @since 0.6a
	 */
	public FTPFutureData<FTPFile> getFile(FTPFilename file) throws IOException {
		return queueDataCommand(FTPTransformation.FILE_TRANSFORMATION, new FTPCommandFileRetrieve(file), file);
	}
	
	/**
	 * Queues a data command to write to a file denoted by the file.
	 * <p>
	 * This method requires a FTPFile with the data that is to
	 * be written to the file.
	 * @param file the file to write to
	 * @param data the data to write to the file
	 * @return a ftp future representing the command
	 * @throws IOException
	 * @since 0.6a
	 */
	public FTPFuture writeFile(FTPFilename file, FTPFile data) throws IOException {
		return queueFileCommand(new FTPCommandFile(FileAction.WRITE, file, data), file);
	}

	/**
	 * Queues a data command to append to a file denoted by the file.
	 * <p>
	 * This method requires a FTPFile with the data that is to
	 * be appended to the file.
	 * @param file the file to append to
	 * @param data the data to append to the file
	 * @return a ftp future representing the command
	 * @throws IOException
	 * @since 0.6a
	 */
	public FTPFuture appendFile(FTPFilename file, FTPFile data) throws IOException {
		return queueFileCommand(new FTPCommandFile(FileAction.APPEND, file, data), file);
	}
	
	/**
	 * Queues a command to delete the file denoted by the file.
	 * @param file the file to delete
	 * @return a ftp future representing the command
	 * @throws IOException
	 * @since 0.6a
	 */
	public FTPFuture deleteFile(FTPFilename file) throws IOException {
		return queueCommand(new FTPCommandFileDelete(file));
	}
	
	/**
	 * Queues a data command to retrieve a list of the objects within
	 * the directory denoted by the directory.
	 * <p>
	 * This method returns a {@link FTPFutureData} object of the
	 * generic {@link FTPEntity}<code>[]</code> which represents the command and the
	 * list being retrieved.
	 * @param file the directory to list
	 * @return a ftp future representing the command
	 * @throws IOException
	 * @since 0.5
	 */
	public FTPFutureData<FTPEntity[]> getFileList(FTPFilename directory) throws IOException {
		return queueDataCommand(FTPTransformation.FILELIST_TRANSFORMATION, new FTPCommandList(directory));
	}

	/**
	 * Queues a data command to retrieve the current directory
	 * <p>
	 * This method returns a {@link FTPFutureData} object of the
	 * generic <code>String</code> which represents the command and the
	 * current directory being retrieved.
	 * @param file the directory to list
	 * @return a ftp future representing the command
	 * @throws IOException
	 * @since 0.6a
	 */
	public FTPFutureData<String> getWorkingDirectory(FTPFilename directory) throws IOException {
		return queueDataCommand(FTPTransformation.ASCII_TRANSFORMATION, new FTPCommandDirectory());
	}
	
	/**
	 * Queues a command to change the current directory to the one indicated
	 * by directory.
	 * @param directory the directory to change to
	 * @return a ftp future representing the command
	 * @throws IOException
	 * @since 0.5
	 */
	public FTPFuture changeWorkingDirectory(FTPFilename directory) throws IOException {
		return queueCommand(new FTPCommandChained(false, new FTPCommandDirectory(DirectoryAction.CHANGE, directory), new FTPCommandDirectory()));
	}
	
	/**
	 * Queues a command to create the directory indicated by directory.
	 * @param directory the directory to create
	 * @return a ftp future representing the command
	 * @throws IOException
	 * @since 0.6a
	 */
	public FTPFuture makeDirectory(FTPFilename directory) throws IOException {
		return queueCommand(new FTPCommandChained(false, new FTPCommandDirectory(DirectoryAction.MAKE, directory), new FTPCommandDirectory()));
	}
	
	/**
	 * Queues a command to delete a directory indicated by directory.
	 * <p>
	 * The directory indicated to be deleted must be completely empty
	 * (on most server implementations) to be able to be successfully
	 * deleted.
	 * @param directory the directory to delete
	 * @return a ftp future representing the command
	 * @throws IOException
	 * @since 0.6a
	 */
	public FTPFuture deleteDirectory(FTPFilename directory) throws IOException {
		return queueCommand(new FTPCommandChained(false, new FTPCommandDirectory(DirectoryAction.REMOVE, directory), new FTPCommandDirectory()));
	}

	/**
	 * Attempts to completely delete the directory specified by directory.
	 * <p>
	 * This method is currently unimplemented.
	 * @param directory the directory to delete
	 * @return a ftp future representing the command
	 * @throws IOException
	 * @since 0.6a
	 */
	public FTPFuture completelyDeleteDirectory(FTPFilename directory) throws IOException {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Queues a data command with the specified {@link FTPTransformation} function.
	 * <p>
	 * The function will be used to convert the generic <code>byte[]</code> result
	 * of {@link FTPDownloadCommand}s to a more usable form.
	 * @param function the transformation to use
	 * @param command the command to queue
	 * @return a ftp future representing the data command
	 * @throws IOException
	 * @since 0.5
	 */
	public <T> FTPFutureData<T> queueDataCommand(final FTPTransformation<T> function, FTPCommand command) throws IOException {
		return queueDataCommand(function, command, null);
	}
	
	/**
	 * Queues a data command with the specified {@link FTPTransformation} function
	 * that will be applied to the specified file.
	 * <p>
	 * The function will be used to convert the generic <code>byte[]</code> result
	 * of {@link FTPDownloadCommand}s to a more usable form.
	 * @param function the transformation to use
	 * @param command the command to queue
	 * @param file the target file
	 * @return a ftp future representing the data command
	 * @throws IOException
	 * @since 0.5
	 */
	public <T> FTPFutureData<T> queueDataCommand(final FTPTransformation<T> function, FTPCommand command, FTPFilename file) throws IOException {
		return new FTPFutureData<T>(queueFileCommand(command, file)) {
			@Override
			protected T formData(byte[] result) throws Exception {
				return function.transform(state, result);
			}
		};
	}

	/**
	 * Queues a file command.
	 * @param command the command to queue
	 * @return a ftp future representing the file command
	 * @throws IOException
	 * @since 0.6a
	 */
	public FTPFuture queueFileCommand(FTPCommand command) throws IOException {
		return queueFileCommand(command, null);
	}
	/**
	 * Queues a file command upon the specified file.
	 * @param command the command to queue
	 * @param file the target file
	 * @return a ftp future representing the file command
	 * @throws IOException
	 * @since 0.6a
	 */
	public FTPFuture queueFileCommand(FTPCommand command, FTPFilename file) throws IOException {
		return queueCommand(new FTPCommandChained(new FTPCommand[] { new FTPCommandType(getFTPType(file)), state.modeCommand, command }));
	}

	/**
	 * Constructs a new absolute {@link FTPFilename} with the path
	 * specified by file.
	 * @param file the path
	 * @return a filename representing the path
	 * @since 0.6a
	 */
	public FTPFilename getAbsoluteFilename(String file) {
		return new FTPFilename("/", file);
	}
	/**
	 * Constructs a new {@link FTPFilename} with the path
	 * specified by file that is relative to the current
	 * directory of the <code>FTPClient</code>.
	 * @param file the path
	 * @return a filename representing the relative path
	 * @since 0.6a
	 */
	public FTPFilename getRelativeFilename(String file) {
		return new FTPFilename(state.workingDirectory, file);
	}
	
	/**
	 * Retrieves the type character for the specified file.
	 * @param file the file to test
	 * @return the type char
	 * @see FTPTypeDecider
	 * @since 0.6a
	 */
	private char getFTPType(FTPFilename file) {
		if (file == null) return FTPTypeDecider.ASCII;
		
		String filename = file.getName();
		int index = filename.lastIndexOf('.');
		if (index > -1) return FTPTypeDecider.decideFTPType(filename.substring(index + 1), index != 0);
		return FTPTypeDecider.decideFTPType("", !filename.isEmpty());
	}
	

	/**
	 * A <code>Thread</code> that handles the parsing and execution of
	 * FTP server responses.
	 * <p>
	 * This <code>Thread</code> uses a <code>InputStream</code> to read
	 * the server responses and direct them to the appropriate executing
	 * command.
	 * 
	 * @author hsun324
	 * @version 0.6a
	 * @since 0.5
	 */
	class FTPStreamThread extends Thread {
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
		 * Flag to stop
		 */
		private boolean stopRequested = false;
		
		/**
		 * A buffer that holds previous lines in multiple line responses.
		 */
		private StringBuffer responseBuffer = new StringBuffer();
		
		/**
		 * Creates a <code>FTPStreamThread</code> with the standard size
		 * character buffer.
		 * @throws IOException
		 */
		public FTPStreamThread() throws IOException {
			this(400);
		}
		
		/**
		 * Creates a <code>FTPStreamThread</code> with the a
		 * character buffer of the specified size.
		 * @throws IOException
		 */
		public FTPStreamThread(int bufferSize) throws IOException {
			this.buffer = new char[bufferSize];
			this.setName("FTPLiteClientStream" + state.host + THREAD_NAME_TICKER);
		}
		
		/**
		 * Waits for a connection to be set and the returns it.
		 * @return the client connection
		 * @since 0.6a
		 */
		public Socket getConnection() {
			synchronized (connectionSync) {
				try {
					while (connection == null && !stopRequested) connectionSync.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return connection;
			}
		}
		
		/**
		 * Method that handles the stream logic and redirects responses to command and handlers.
		 */
		@Override
		public void run() {
			try {
				Socket socket = getConnection();
				if (socket != null) {
					this.inputStream = socket.getInputStream();
					this.reader = new InputStreamReader(this.inputStream, FTPCharset.ASCII);
					
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
									
									if (!FTPHandlerRegistry.tryGlobalHandle(state, response) && state.currentFuture != null)
										if (state.currentFuture.command.pushResponse(state, response))
											state.currentFuture = null;
								} else responseBuffer.append(content).append("\n");
								continue;
							}
							responseBuffer.append(line).append("\n");
						}
					}
					reader.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		/**
		 * Requests this <code>FTPStreamThread</code> to stop.
		 */
		public void requestStop() {
			stopRequested = true;
			synchronized (connectionSync) {
				connectionSync.notifyAll();
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

	/**
	 * A <code>Thread</code> that handles the execution of queued
	 * commands.
	 * @author hsun324
	 * @version 0.6a
	 * @since 0.5
	 */
	class FTPQueueThread extends Thread {
		/**
		 * Creates a <code>FTPQueueThread</code>.
		 */
		protected FTPQueueThread() {
			this.setName("FTPLiteClientQueue" + state.host + THREAD_NAME_TICKER);
		}
		
		/**
		 * Flag to stop
		 */
		private boolean stopRequested = false;
		
		/**
		 * Waits for a command to be in the queue and then removes and returns the command.
		 * @return the first command in the queue
		 * @throws IOException
		 */
		protected FTPFuture pullFuture() throws IOException {
			synchronized (commandQueue) {
				try {
					while (commandQueue.size() == 0 && !stopRequested)
						commandQueue.wait();
					return stopRequested ? null : commandQueue.remove(0);
				} catch (InterruptedException e) {
					throw new IOException("failed pull", e);
				}
			}
		}
		
		/**
		 * Method that handles the queue logic and executes commands in order.
		 */
		@Override
		public void run() {
			while (!stopRequested) {
				try {
					FTPFuture future = pullFuture();
					
					if (future != null) {
						state.currentFuture = future;
						future.execute();
						future.waitUntilResult();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
		
		/**
		 * Requests this <code>FTPQueueThread</code> to stop.
		 */
		public void requestStop() {
			stopRequested = true;
		}
	}
}
