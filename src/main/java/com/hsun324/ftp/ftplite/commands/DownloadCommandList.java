package com.hsun324.ftp.ftplite.commands;

import com.hsun324.ftp.ftplite.FTPObject;
import com.hsun324.ftp.ftplite.FTPResponse;
import com.hsun324.ftp.ftplite.client.FTPInterface;
import com.hsun324.ftp.ftplite.client.FTPInterface.ClientState;
import com.hsun324.ftp.ftplite.client.FTPInterface.Feature;

/**
 * This {@link Command} handles the list
 * LIST/MLSD command.
 * @author hsun324
 * @version 0.7
 */
public class DownloadCommandList extends DownloadCommand {
	private final String directory;
	public DownloadCommandList() {
		this(null);
	}
	public DownloadCommandList(FTPObject directory) {
		// TODO: Current Directory Test
		this.directory = directory != null ? " " + directory.getPath() : "";
	}
	
	@Override
	public String getCommandContent(FTPInterface inter) {
		if (inter.isFeatureSupported(Feature.METADATA_LIST)) return "MLSD" + directory;
		return "LIST" + directory;
	}
	@Override
	public boolean isValidContext(FTPInterface inter) {
		return inter.getClientState() == ClientState.READY;
	}
	@Override
	public byte[] processData(FTPInterface inter, FTPResponse response, byte[] data) {
		return data;
	}
}
