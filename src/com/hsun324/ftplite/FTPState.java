package com.hsun324.ftplite;


public class FTPState {
	protected FTPState(FTPClient client, String host, int port) {
		this.client = client;
		this.host = host;
		this.port = port;
	}
	
	protected final Object connectionSemaphore = new Object();
	
	public final FTPClient client;
	public final String host;
	public final int port;
	
	public boolean connected = false;

	public FTPFuture currentFuture = null;
	
	public String welcomeMessage = "";
	
	public boolean authStarted = false;
	public boolean authCompleted = false;
	public boolean authPassword = false;

	public FTPCommand modeCommand = null;
	public boolean modeActive = false;

	public String dataHost = "";
	public int dataPort = 0;
	
	public void reset() {
		this.authStarted = false;
		this.authCompleted = false;
		this.authPassword = false;
		
		this.modeCommand = null;
		this.modeActive = false;
		
		this.dataHost = "";
		this.dataPort = 0;
	}
}
