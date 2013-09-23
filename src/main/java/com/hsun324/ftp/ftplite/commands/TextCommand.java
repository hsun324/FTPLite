package com.hsun324.ftp.ftplite.commands;

import java.io.IOException;

import com.hsun324.ftp.ftplite.FTPFile.FTPFiletype;
import com.hsun324.ftp.ftplite.client.FTPInterface;

public abstract class TextCommand extends Command {
	@Override
	public void execute(FTPInterface inter) throws IOException {
		StringBuilder builder = new StringBuilder(getCommandContent(inter));
		System.out.println(builder.toString());
		inter.writeBytes(builder.append("\r\n").toString().getBytes(FTPFiletype.ASCII.getCharset()));
	}
	
	/**
	 * Gets the content of the command to be sent to the server.
	 * <p>
	 * This method is used by the default <code>execute</code> methods
	 * in <code>FTPCommand</code>, {@link FTPDownloadCommand},
	 * and {@link FTPUploadCommand}.
	 * @param state the current client state
	 * @return the command content
	 */
	public String getCommandContent(FTPInterface inter) {
		return "NOOP";
	}
}
