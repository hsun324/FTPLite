package com.hsun324.ftplite;

import com.hsun324.ftplite.commands.FTPCommandNonPrint;
import com.hsun324.ftplite.commands.FTPCommandPassive;
import com.hsun324.ftplite.commands.FTPCommandType;


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

	public FTPCommand printCommand = new FTPCommandNonPrint();
	
	public FTPCommand modeCommand = new FTPCommandPassive();
	public boolean modeActive = false;
	
	public boolean typeImage = false;
	
	public boolean featureExtPassive = false;

	public boolean featureModificationTime = false;
	
	public boolean featureFileMetadata = false;
	public boolean featureFileSize = false;
	public boolean featureUTF8 = false;
	
	public boolean acceptUTF8 = false;

	public String dataHost = "";
	public int dataPort = 0;

	public String system = "";

	
	public void reset() {
		this.authStarted = false;
		this.authCompleted = false;
		this.authPassword = false;
		
		this.printCommand = new FTPCommandNonPrint();
		
		this.modeCommand = new FTPCommandPassive();
		this.modeActive = false;
		
		this.typeImage = false;
		
		this.featureExtPassive = false;
		
		this.featureModificationTime = false;

		this.featureFileMetadata = false;
		this.featureFileSize = false;
		this.featureUTF8 = false;
		
		this.acceptUTF8 = false;
		
		this.dataHost = "";
		this.dataPort = 0;
	}
}
