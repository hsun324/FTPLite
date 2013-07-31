package com.hsun324.ftplite;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hsun324.ftplite.commands.*;

public class FTPClient {
	public static final int DEFAULT_FTP_SERVER_PORT = 21;
	private static final Pattern FILENAME_REGEX_PATTERN = Pattern.compile("^[-\\w,\\s/]*/?([-\\w,\\s.]*)$");
	private static final Pattern FILEEXT_REGEX_PATTERN = Pattern.compile("^([-\\w,\\s]*)\\.([A-Za-z]+)$");
	
	public FTPClient(String host) {
		this(host, DEFAULT_FTP_SERVER_PORT);
	}
	public FTPClient(String host, int port) {
		super();
		this.state = new FTPState(this, host, port);
	}
	protected final FTPState state;
	
	protected final List<FTPFuture> commandQueue = new ArrayList<FTPFuture>();
	
	private Socket connection = null;
	private FTPStreamThread responseThread = null;
	private OutputStream commandStream = null;
	
	private FTPQueueThread queueThread = null;

	protected OutputStream getCommandStream() {
		return commandStream;
	}
	
	public FTPFuture queueCommand(FTPCommand command) throws IOException {
		synchronized (commandQueue) {
			if (command.isExecuted()) throw new IllegalStateException();
			FTPFuture future = new FTPFuture(this, command);
			commandQueue.add(future);
			commandQueue.notify();
			return future;
		}
	}
	
	public synchronized FTPFuture connect(String user, String password) throws UnknownHostException, IOException {
		FTPCommand firstCommand = null;
		
		if (!state.connected) {
			connection = new Socket(state.host, state.port);
			
			queueThread = new FTPQueueThread();
			queueThread.start();
			
			responseThread = new FTPStreamThread(this, connection);
			responseThread.start();
			
			commandStream = connection.getOutputStream();
			
			firstCommand = new FTPCommandConnect();
		} else firstCommand = new FTPCommandReinitialize();
		
		return queueCommand(new FTPCommandChained(new FTPCommand[]{
			firstCommand,
			new FTPCommandUser(user),
			new FTPCommandPassword(password),
			new FTPCommandSystem(),
			new FTPCommandFeatures(),
			new FTPCommandUTF8()
		}));
	}
	
	public void quit() throws IOException {
		queueCommand(new FTPCommandQuit());
	}
	public synchronized void close() {
		try {
			responseThread.requestStop();
			queueThread.requestStop();
			commandStream.close();
			
			state.reset();
			state.connected = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setActiveMode() throws IOException {
		state.modeActive = true;
		state.modeCommand = new FTPCommandActive();
	}
	public void setPassiveMode() throws IOException {
		state.modeActive = false;
		state.modeCommand = new FTPCommandPassive();
	}

	public FTPFutureData<String> getFile(String file) throws IOException {
		return queueDataCommand(FTPTransformation.FILE_TRANSFORMATION, new FTPCommandRetreive(file), file);
	}
	public FTPFuture putFile(String file, String data) throws IOException {
		return queueFileCommand(new FTPCommandPut(file, data), file);
	}
	public FTPFutureData<FTPEntity[]> getFileList(String directory) throws IOException {
		return queueDataCommand(new FTPTransformation.FileListTransformation(state.workingDirectory), new FTPCommandList(directory));
	}
	
	public FTPFuture changeWorkingDirectory(String directory) throws IOException {
		// TODO: relative/absolute path resolution
		return queueCommand(new FTPCommandCWD(directory));
	}
	public FTPFuture queueFileCommand(FTPCommand command) throws IOException {
		return queueFileCommand(command, null);
	}
	public FTPFuture queueFileCommand(FTPCommand command, String file) throws IOException {
		return queueCommand(new FTPCommandChained(new FTPCommand[] { new FTPCommandType(getFTPType(file)), state.printCommand, state.modeCommand, command }));
	}
	public <T> FTPFutureData<T> queueDataCommand(final FTPTransformation<T> function, FTPCommand command) throws IOException {
		return queueDataCommand(function, command, null);
	}
	public <T> FTPFutureData<T> queueDataCommand(final FTPTransformation<T> function, FTPCommand command, String file) throws IOException {
		return new FTPFutureData<T>(queueFileCommand(command, file)) {
			@Override
			protected T formData(byte[] result) throws Exception {
				return function.transform(result);
			}
		};
	}
	
	private char getFTPType(String file) {
		String extension = null;
		boolean hasName = false;
		if (file != null) {
			Matcher matcher = FILENAME_REGEX_PATTERN.matcher(file);
			if (matcher.find()) {
				matcher = FILEEXT_REGEX_PATTERN.matcher(matcher.group(1));
				if (matcher.find()) {
					hasName = !matcher.group(1).trim().isEmpty();
					extension = matcher.group(2);
				}
			}
		}
		return FTPTypeDecider.decideFTPType(extension, hasName);
	}
	
	/*public <T> FTPFutureData<T> sendTransformCommand(final FTPTransformation<T> function, FTPCommand command) throws IOException {
		return new FTPFutureData<T>(queueCommand(command)) {
			@Override
			protected T formData(byte[] result) throws Exception {
				return function.transform(result);
			}
		};
	}*/
	
	class FTPQueueThread extends Thread {
		private boolean stopRequested = false;
		
		protected FTPFuture pullFuture() throws IOException {
			synchronized (commandQueue) {
				try {
					while (commandQueue.size() == 0)
						commandQueue.wait();
					return commandQueue.remove(0);
				} catch (InterruptedException e) {
					throw new IOException(e);
				}
			}
		}
		@Override
		public void run() {
			while (!stopRequested) {
				try {
					FTPFuture future = state.currentFuture = pullFuture();
					
					future.execute();
					future.waitUntilResult();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
		public void requestStop() {
			stopRequested = true;
		}
	}
}
