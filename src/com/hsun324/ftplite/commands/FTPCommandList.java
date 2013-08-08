package com.hsun324.ftplite.commands;

import com.hsun324.ftplite.FTPCommand;
import com.hsun324.ftplite.FTPDownloadCommand;
import com.hsun324.ftplite.FTPFilename;
import com.hsun324.ftplite.FTPResponse;
import com.hsun324.ftplite.FTPState;

/**
 * This {@link FTPCommand} handles the list
 * LIST/MLSD command.
 * @author hsun324
 * @version 0.6a
 * @since 0.5
 */
public class FTPCommandList extends FTPDownloadCommand {
	private final String directory;
	public FTPCommandList() {
		this(null);
	}
	public FTPCommandList(FTPFilename directory) {
		// TODO: Current Directory Test
		this.directory = directory != null ? " " + directory.getPath() : "";
	}
	
	@Override
	public String getCommandContent(FTPState state) {
		if (state.featureFileMetadata) return "MLSD" + directory;
		return "LIST" + directory;
	}
	@Override
	public boolean isValidContext(FTPState state) {
		return state.authCompleted && state.modeCommand != null;
	}
	@Override
	public byte[] processData(FTPState state, FTPResponse response, byte[] data) {
		return data;
	}
}
