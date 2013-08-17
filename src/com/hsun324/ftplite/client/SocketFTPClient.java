package com.hsun324.ftplite.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.hsun324.ftplite.FTPCommand;
import com.hsun324.ftplite.FTPCommandChained;
import com.hsun324.ftplite.FTPFuture;
import com.hsun324.ftplite.FTPResult;
import com.hsun324.ftplite.FTPState;
import com.hsun324.ftplite.commands.*;

/**
 * This class is an implementation of a {@link
 * FTPClient} over a insecure <code>Socket</code>
 * layer.
 * 
 * @author hsun324
 * @version 0.7
 * @since 0.7
 */
public class SocketFTPClient extends AbstractFTPClient {
	// TODO: Exception handling everywhere
	
	/**
	 * Default FTP connection port
	 */
	public static final int DEFAULT_FTP_SERVER_PORT = 21;
	
	/**
	 * Creates a <code>FTPClient</code> bound to the provided host using the default FTP port.
	 * @param host address to connect to
	 */
	public SocketFTPClient(String host) {
		this(host, DEFAULT_FTP_SERVER_PORT);
	}
	
	/**
	 * Creates a <code>FTPClient</code> bound to the provided host using the provided port.
	 * @param host address to connect to
	 * @param port port to connect to
	 */
	public SocketFTPClient(String host, int port) {
		super();
		this.uuid = UUID.randomUUID();
		this.state = new FTPState(this, host, port);
	}
	
	/**
	 * The state of the current <code>FTPClient</code>.
	 */
	protected final FTPState state;
	
	/**
	 * The UUID of the current <code>FTPClient</code>.
	 */
	protected UUID uuid;
	
	/**
	 * The queue of commands waiting to be executed.
	 * <p>
	 * This <code>List</code> is not inherently synchronized, therefore all accesses to this
	 * <code>List</code> should be synchronized externally.
	 */
	protected final List<FTPFuture> commandQueue = new ArrayList<FTPFuture>();
	
	/**
	 * Connection access semaphore
	 */
	final Object connectionSync = new Object();
	
	/**
	 * The underlying <code>Socket</code> used to handle the control connection for two-way
	 * communication with the server.
	 */
	Socket connection = null;
	
	/**
	 * The {@link InputStreamThread} that handles the receiving of command responses from the server.
	 */
	private InputStreamThread responseThread = null;
	
	/**
	 * The <code>OutputStream</code> that is connected to the server used to send commands.
	 */
	private OutputStream commandStream = null;

	/**
	 * The {@link FTPQueueThread} that handles the retrieval and execution of queued commands.
	 */
	private FTPQueueThread queueThread = null;

	private boolean stopRequested = false;

	@Override
	protected FTPState getState() {
		return state;
	}

	@Override
	protected List<FTPFuture> getCommandQueue() {
		return commandQueue;
	}
	
	@Override
	public String getUUIDString() {
		return uuid.toString();
	}
	
	@Override
	public OutputStream getCommandStream() {
		return commandStream;
	}
	
	@Override
	protected FTPFuture queueClosableCommandInner(FTPCommand command) throws IOException {
		synchronized (commandQueue) {
			if (command.isExecuted()) throw new IllegalStateException("already executed command");
			
			FTPFuture future = new FTPFuture(this, state, command);
			commandQueue.add(future);
			commandQueue.notify();
			return future;
		}
	}
	
	public synchronized FTPFuture connect(String user, String password) throws IOException {
		try {
			FTPCommand firstCommand = null;
			
			if (!state.connected) {
				uuid = UUID.randomUUID();
				
				queueThread = new FTPQueueThread(this);
				queueThread.start();
				
				responseThread = new InputStreamThread(this);
				responseThread.start();
				
				firstCommand = new FTPCommandConnect();
			} else firstCommand = new FTPCommandReinitialize();
			
			FTPFuture future = queueClosableCommand(new FTPCommandChained(new FTPCommand[]{
				firstCommand,
				new FTPCommandUser(user),
				new FTPCommandPassword(password),
				new FTPCommandSystem(),
				new FTPCommandFeatures(),
				new FTPCommandDirectory()
			}));
			
			if (!state.connected) {
				new SocketConnectThread(state.host, state.port).start();
			}
			
			return future;
		} catch (Exception e) {
			close();
			throw new IOException("could not make connection", e);
		}
	}
	
	public synchronized void close() {
		try {
			stopRequested = true;
			
			responseThread = null;
			queueThread = null;
			
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
				future.quitExecution();
				if (!future.isResultSet()) future.setResult(FTPResult.FAILED);
			}
			
			synchronized (connectionSync) {
				connectionSync.notifyAll();
			}
			
			state.reset();
			state.connected = false;
			stopRequested = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected InputStream getInputStream() throws IOException {
		synchronized (connectionSync) {
			try {
				while (connection == null && !stopRequested) connectionSync.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return connection.getInputStream();
		}
	}

	@Override
	protected OutputStream getOutputStream() throws IOException {
		return getConnection().getOutputStream();
	}

	@Override
	protected boolean isStopRequested() {
		return stopRequested;
	}
	
	private Socket getConnection() {
		synchronized (connectionSync) {
			try {
				while (connection == null && !stopRequested) connectionSync.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return connection;
		}
	}

	class SocketConnectThread extends Thread {
		public SocketConnectThread(String host, int port) {
			super();
			
			this.setName("FTPL-SCT-" + state.host + "-" + getUUIDString());
			this.setDaemon(true);
			
			this.host = host;
			this.port = port;
		}
		
		private final String host;
		private final int port;
		
		@Override
		public void run() {
			try {
				synchronized (connectionSync) {
					connection = new Socket(host, port);
					commandStream = connection.getOutputStream();
					connectionSync.notifyAll();
				}
			} catch (IOException e) {
				// TODO: Exception handling
				e.printStackTrace();
			}
		}
	}
}
