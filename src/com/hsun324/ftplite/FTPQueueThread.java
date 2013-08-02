package com.hsun324.ftplite;

import java.io.IOException;

class FTPQueueThread extends Thread {
	private final FTPClient client;
	protected FTPQueueThread(FTPClient client) {
		this.client = client;
	}
	private boolean stopRequested = false;
	
	protected FTPFuture pullFuture() throws IOException {
		synchronized (client.commandQueue) {
			try {
				while (client.commandQueue.size() == 0)
					client.commandQueue.wait();
				return client.commandQueue.remove(0);
			} catch (InterruptedException e) {
				throw new IOException(e);
			}
		}
	}
	@Override
	public void run() {
		while (!stopRequested) {
			try {
				FTPFuture future = client.state.currentFuture = pullFuture();
				
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