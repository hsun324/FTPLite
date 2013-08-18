package com.hsun324.ftp.ftplite.client;

import java.io.IOException;
import java.util.List;

import com.hsun324.ftp.ftplite.FTPFuture;

/**
 * A <code>Thread</code> that handles the execution of queued
 * commands.
 * @author hsun324
 * @version 0.7
 */
class FTPQueueThread extends Thread {
	/**
	 * This thread's client.
	 */
	private final AbstractFTPClient client;

	/**
	 * Creates a <code>FTPQueueThread</code>.
	 * @param client the client this thread runs for
	 */
	protected FTPQueueThread(AbstractFTPClient client) {
		this.client = client;
		this.setName("FTPL-CQ-" + client.getState().host + "-" + client.getUUIDString());
	}
	
	/**
	 * Waits for a command to be in the queue and then removes and returns the command.
	 * @return the first command in the queue
	 * @throws IOException
	 */
	protected FTPFuture pullFuture() throws IOException {
		List<FTPFuture> queue = client.getCommandQueue();
		synchronized (queue) {
			try {
				while (queue.size() == 0 && !client.isStopRequested())
					queue.wait();
				return client.isStopRequested() ? null : queue.remove(0);
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
		while (!client.isStopRequested()) {
			try {
				FTPFuture future = pullFuture();
				
				if (future != null) {
					client.getState().currentFuture = future;
					future.execute();
					future.waitUntilResult();
				}
			} catch (IOException e) {
				// TODO: Exceptions
			}
		}
		
	}
}