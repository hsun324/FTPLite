package com.hsun324.ftp.ftplite.client;

public class SocketThread extends Thread {
	private final SocketFTPClient client;
	public SocketThread(SocketFTPClient client) {
		super();
		
		this.client = client;
		this.setName("FTPL-SCT-" + client.getUniqueString());
		this.setDaemon(true);
	}
	
	@Override
	public void run() {
		try {
			client.setConnection(client.getSocketFactory().call());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}