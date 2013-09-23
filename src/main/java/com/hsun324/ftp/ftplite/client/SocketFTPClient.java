package com.hsun324.ftp.ftplite.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import com.hsun324.ftp.ftplite.FTPFuture;
import com.hsun324.ftp.ftplite.FTPObject;
import com.hsun324.ftp.ftplite.FTPResult;
import com.hsun324.ftp.ftplite.FTPFile.FTPFiletype;
import com.hsun324.ftp.ftplite.client.FTPInterface.ClientState;
import com.hsun324.ftp.ftplite.client.FTPInterface.Feature;
import com.hsun324.ftp.ftplite.commands.*;

/**
 * This class is an implementation of a {@link
 * FTPClient} over a insecure <code>Socket</code>
 * layer.
 * 
 * @author hsun324
 * @version 0.7
 */
public class SocketFTPClient extends FTPClient {
	// TODO: Exception handling everywhere
	
	/**
	 * Default FTP connection port
	 */
	public static final int DEFAULT_FTP_SERVER_PORT = 21;
	
	private final String host;
	private final int port;
	
	private final List<FTPFuture> queue = new ArrayList<FTPFuture>();
	
	private Socket connection = null;
	private final Object connectionSync = new Object();

	private final FTPClientThread thread;
	
	private ClientState clientState = ClientState.UNOPENED;
	private ModeCommand modeCommand = new ModeCommandPassive();
	private String dataHost = null;
	private int dataPort = 0;

	private final UUID uuid;
	
	private FTPFuture currentFuture = null;
	private Object futureSync = new Object();
	
	private FTPObject currentDirectory = FTPObject.ROOT_DIRECTORY;
	private FTPFiletype currentFiletype = FTPFiletype.ASCII;
	
	private final SocketFTPClientInterface inter = new SocketFTPClientInterface();

	private final Map<Feature, Boolean> supportedFeatures = new HashMap<Feature, Boolean>();
	private String[] metadataParams = new String[0];
	
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
		this.host = host;
		this.port = port;
		this.uuid = UUID.randomUUID();
		
		this.thread = new FTPClientThread(this);
	}
	
	@Override
	protected FTPFuture queueCommandUnsafe(Command command) throws IOException {
		synchronized (queue) {
			FTPFuture future = new FTPFuture(this, inter, command);
			queue.add(future);
			queue.notify();
			return future;
		}
	}

	@Override
	protected void beginConnection() {
		thread.start();
		new SocketThread(this).start();
	}
	
	@Override
	protected void closeConnection() throws IOException {
		FTPFuture future = inter.getFuture();
		if (future != null) {
			future.quitExecution();
			if (!future.isResultSet()) future.setResult(FTPResult.FAILED);
		}

		thread.interrupt();
		
		synchronized (connectionSync) {
			connectionSync.notifyAll();
		}
		
		if (connection != null) {
			connection.close();
			connection = null;
		}
	}
	
	private InputStream connectionInputStream = null;
	@Override
	protected InputStream getInputStream() throws IOException {
		if (inter.isClosed()) throw new IOException("client closed");
		if (connectionInputStream == null) connectionInputStream = getConnection().getInputStream();
		return connectionInputStream; 
	}
	
	private OutputStream connectionOutputStream = null;
	@Override
	protected OutputStream getOutputStream() throws IOException {
		if (inter.isClosed()) throw new IOException("client closed");
		if (connectionOutputStream == null) connectionOutputStream = getConnection().getOutputStream();
		return connectionOutputStream; 
	}
	
	protected Socket getConnection() throws IOException {
		synchronized (connectionSync) {
			try {
				if (inter.isClosed()) throw new IOException("client closed");
				while (connection == null && !inter.isClosing()) connectionSync.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
				return null;
			}
			return connection;
		}
	}
	protected void setConnection(Socket socket) {
		synchronized (connectionSync) {
			if (connection != null) throw new IllegalStateException();
			connection = socket;
			connectionSync.notifyAll();
		}
	}

	protected Callable<Socket> getSocketFactory() {
		return new Callable<Socket>() {
			@Override
			public Socket call() throws Exception {
				return new Socket(host, port);
			}
		};
	}
	
	@Override
	protected FTPInterface getInterface() {
		return inter;
	}
	
	public class SocketFTPClientInterface implements FTPInterface {
		@Override
		public String getHost() {
			return host;
		}
		@Override
		public int getPort() {
			return port;
		}
	
		@Override
		public boolean isConnected() {
			synchronized (clientState) {
				return clientState.isConnected();
			}
		}
		@Override
		public boolean isClosing() {
			synchronized (clientState) {
				return clientState == ClientState.CLOSING;
			}
		}
		@Override
		public boolean isClosed() {
			synchronized (clientState) {
				return clientState == ClientState.CLOSED;
			}
		}
	
		@Override
		public void setClientState(ClientState state) {
			synchronized (clientState) {
				clientState = state;
			}
		}
		@Override
		public ClientState getClientState() {
			synchronized (clientState) {
				return clientState;
			}
		}
	
		@Override
		public void setModeCommand(ModeCommand command) {
			synchronized (modeCommand) {
				modeCommand = command;
			}
		}
		@Override
		public ModeCommand getModeCommand() {
			synchronized (modeCommand) {
				return modeCommand;
			}
		}
	
		@Override
		public void setFeatureSupported(Feature feature, boolean supported) {
			synchronized (supportedFeatures) {
				supportedFeatures.put(feature, supported);
			}
		}
		@Override
		public boolean isFeatureSupported(Feature feature) {
			synchronized (supportedFeatures) {
				Boolean supported = supportedFeatures.get(feature);
				if (supported != null) return supported;
				return false;
			}
		}
	
		@Override
		public void setCurrentDirectory(FTPObject directory) {
			synchronized (currentDirectory) {
				currentDirectory = directory;
			}
		}
		@Override
		public FTPObject getCurrentDirectory() {
			synchronized (currentDirectory) {
				return currentDirectory;
			}
		}
	
		@Override
		public void setCurrentFiletype(FTPFiletype filetype) {
			currentFiletype = filetype;
		}
		@Override
		public FTPFiletype getCurrentFiletype() {
			return currentFiletype;
		}
	
		@Override
		public void setFuture(FTPFuture future) {
			synchronized (futureSync) {
				currentFuture = future;
			}
		}
		@Override
		public FTPFuture getFuture() {
			synchronized (futureSync) {
				return currentFuture;
			}
		}
		@Override
		public void setMetadataParameters(String[] params) {
			metadataParams = params;
		}
		@Override
		public String[] getMetadataParameters() {
			return metadataParams;
		}
	
		@Override
		public UUID getUUID() {
			return uuid;
		}
		@Override
		public String getUUIDString() {
			return uuid.toString();
		}
		
		@Override
		public String getUniqueString() {
			return SocketFTPClient.this.getUniqueString();
		}
		
		@Override
		public void writeBytes(byte[] bytes) throws IOException {
			getOutputStream().write(bytes);
		}
		
		@Override
		public void setDataHost(String host) {
			dataHost = host;
		}
		@Override
		public String getDataHost() {
			return dataHost;
		}
		@Override
		public void setDataPort(int port) {
			dataPort = port;
		}
		@Override
		public int getDataPort() {
			return dataPort;
		}
		
		@Override
		public List<FTPFuture> getCommandQueue() {
			return queue;
		}
		
		@Override
		public void close() throws IOException {
			closeConnection();
		}
	}
	
	@Override
	protected String getUniqueString() {
		return host + "_" + uuid.toString();
	}

	@Override
	public FTPFuture completelyDeleteDirectory(FTPObject directory) throws IOException {
		throw new UnsupportedOperationException();
	}
}
