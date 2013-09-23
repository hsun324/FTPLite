package com.hsun324.ftp.ftplite.client;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import com.hsun324.ftp.ftplite.FTPFile.FTPFiletype;
import com.hsun324.ftp.ftplite.FTPFuture;
import com.hsun324.ftp.ftplite.FTPObject;
import com.hsun324.ftp.ftplite.commands.ModeCommand;

public interface FTPInterface {

	public String getHost();
	public int getPort();
	
	public boolean isConnected();
	public boolean isClosing();
	public boolean isClosed();
	
	public void setClientState(ClientState state);
	public ClientState getClientState();
	
	public void setModeCommand(ModeCommand modeCommand);
	public ModeCommand getModeCommand();

	public void setFeatureSupported(Feature feature, boolean supported);
	public boolean isFeatureSupported(Feature feature);
	
	public void setMetadataParameters(String[] params);
	public String[] getMetadataParameters();
	
	public void setCurrentDirectory(FTPObject currentDirectory);
	public FTPObject getCurrentDirectory();

	public void setCurrentFiletype(FTPFiletype currentFiletype);
	public FTPFiletype getCurrentFiletype();
	
	public void setFuture(FTPFuture future);
	public FTPFuture getFuture();
	
	public void setDataHost(String host);
	public String getDataHost();
	
	public void setDataPort(int port);
	public int getDataPort();
	
	public UUID getUUID();
	public String getUUIDString();
	public String getUniqueString();
	
	public void writeBytes(byte[] bytes) throws IOException;

	public List<FTPFuture> getCommandQueue();
	public void close() throws IOException;
	
	public static enum ClientState {
		UNOPENED(false),
		REQUESTING_USERNAME(true),
		REQUESTING_PASSWORD(true),
		READY(true),
		CLOSING(false),
		CLOSED(false);
		
		private final boolean connected;
		private ClientState() {
			this(true);
		}
		private ClientState(boolean connected) {
			this.connected = connected;
		}
		
		public boolean isConnected() {
			return connected;
		}
	}
	public static enum Feature {
		EXTENDED_PASSIVE, MODIFICATION_TIME, METADATA_LIST, FILE_SIZE, UTF8;
	}
}
