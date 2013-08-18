package com.hsun324.ftp.ftplite;

import com.hsun324.ftp.ftplite.client.FTPClient;
import com.hsun324.ftp.ftplite.commands.FTPCommandPassive;

/**
 * This class represents the state of the client it is attached to.
 * <p>
 * This state is never removed from the client. The <code>reset()</code>
 * method allows for this state to return to a default mode.
 * @author hsun324
 * @version 0.7
 */
public class FTPState {
	/**
	 * Creates a <code>FTPState</code> bound to the provided client
	 * with the specified host and port.
	 * @param client the client
	 * @param host the host
	 * @param port the port
	 */
	public FTPState(FTPClient client, String host, int port) {
		this.client = client;
		this.host = host;
		this.port = port;
	}
	
	/**
	 * This state's client
	 */
	public final FTPClient client;
	/**
	 * This state's client's host
	 */
	public final String host;
	/**
	 * This state's client's port
	 */
	public final int port;

	/**
	 * Flag indicating whether the client is connected.
	 */
	public boolean connected = false;


	/**
	 * This state's currently executing future.
	 */
	public FTPFuture currentFuture = null;
	

	/**
	 * The server welcome message.
	 */
	public String welcomeMessage = "";

	/**
	 * Flag indicating whether the authentication
	 * process has started.
	 */
	public boolean authStarted = false;
	/**
	 * Flag indicating whether the authentication
	 * process has completed.
	 */
	public boolean authCompleted = false;
	/**
	 * Flag indicating whether the authentication
	 * process requires a password.
	 */
	public boolean authPassword = false;
	
	/**
	 * The current transmission mode command.
	 */
	public FTPModeCommand modeCommand = new FTPCommandPassive();
	

	/**
	 * Flag indicating whether the transmission
	 * is binary or ASCII.
	 */
	public boolean typeImage = false;
	

	/**
	 * The current working directory.
	 */
	public FTPFilename workingDirectory = FTPFilename.ROOT_DIRECTORY;

	/**
	 * Flag indicating whether the extended passive feature is supported.
	 */
	public boolean featureExtPassive = false;

	/**
	 * Flag indicating whether the file modification time feature is supported.
	 */
	public boolean featureModificationTime = false;

	/**
	 * Flag indicating whether the metadata file list feature is supported.
	 */
	public boolean featureFileMetadata = false;
	/**
	 * List of metadata list parameters.
	 */
	public String[] featureFileMetadataParams = null;
	/**
	 * Flag indicating whether the file size feature is supported.
	 */
	public boolean featureFileSize = false;
	/**
	 * Flag indicating whether the UTF-8 names feature is supported.
	 */
	public boolean featureUTF8 = false;

	/**
	 * Flag indicating whether the server is in UTF-8 mode.
	 */
	public boolean acceptUTF8 = false;

	/**
	 * The current data connection host.
	 */
	public String dataHost = "";
	/**
	 * The current data connection port.
	 */
	public int dataPort = 0;

	/**
	 * The current server's system response.
	 */
	public String system = "";
	
	/**
	 * Resets this state.
	 */
	public void reset() {
		this.authStarted = false;
		this.authCompleted = false;
		this.authPassword = false;
		
		this.modeCommand = new FTPCommandPassive();
		
		this.typeImage = false;
		
		this.workingDirectory = FTPFilename.ROOT_DIRECTORY;
		
		this.featureExtPassive = false;
		
		this.featureModificationTime = false;

		this.featureFileMetadata = false;
		this.featureFileMetadataParams = null;
		this.featureFileSize = false;
		this.featureUTF8 = false;
		
		this.acceptUTF8 = false;
		
		this.dataHost = "";
		this.dataPort = 0;
	}
}
