package com.hsun324.ftp.ftplite.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.hsun324.ftp.ftplite.FTPCommand;
import com.hsun324.ftp.ftplite.FTPCommandChained;
import com.hsun324.ftp.ftplite.FTPEntity;
import com.hsun324.ftp.ftplite.FTPFile;
import com.hsun324.ftp.ftplite.FTPFilename;
import com.hsun324.ftp.ftplite.FTPFuture;
import com.hsun324.ftp.ftplite.FTPFutureData;
import com.hsun324.ftp.ftplite.FTPState;
import com.hsun324.ftp.ftplite.FTPTransformation;
import com.hsun324.ftp.ftplite.FTPTypeDecider;
import com.hsun324.ftp.ftplite.commands.FTPCommandActive;
import com.hsun324.ftp.ftplite.commands.FTPCommandDirectory;
import com.hsun324.ftp.ftplite.commands.FTPCommandFile;
import com.hsun324.ftp.ftplite.commands.FTPCommandFileDelete;
import com.hsun324.ftp.ftplite.commands.FTPCommandFileRetrieve;
import com.hsun324.ftp.ftplite.commands.FTPCommandList;
import com.hsun324.ftp.ftplite.commands.FTPCommandPassive;
import com.hsun324.ftp.ftplite.commands.FTPCommandQuit;
import com.hsun324.ftp.ftplite.commands.FTPCommandType;
import com.hsun324.ftp.ftplite.commands.FTPCommandDirectory.DirectoryAction;
import com.hsun324.ftp.ftplite.commands.FTPCommandFile.FileAction;

/**
 * This class implements the shared components of
 * {@link FTPClient} implementations.
 * 
 * @author hsun324
 * @version 0.7
 * @since 0.7
 */
public abstract class AbstractFTPClient implements FTPClient {
	
	private final Object queueSync = new Object();
	
	protected abstract FTPState getState();
	protected abstract List<FTPFuture> getCommandQueue();
	
	public abstract String getUUIDString();
	
	@Override
	public abstract OutputStream getCommandStream();
	


	@Override
	public abstract FTPFuture connect(String user, String password) throws IOException;

	@Override
	public abstract void close();

	@Override
	public FTPFuture queueCommand(FTPCommand command) throws IOException {
		synchronized (queueSync) {
			if (!getState().connected) throw new IllegalStateException("conn closed");
			return queueClosableCommand(command);
		}
	}
	protected abstract FTPFuture queueClosableCommandInner(FTPCommand command) throws IOException;
	
	/**
	 * Adds a command to the command queue for later execution and returns an {@link FTPFuture} element representing
	 * the progress of the execution of the current command.
	 * <p>
	 * This method does not check the status of the connection or queue thread before adding the command to the
	 * queue.
	 * @param command the command to queue
	 * @return a ftp future representing the command
	 * @throws IOException
	 * @since 0.5
	 */
	protected FTPFuture queueClosableCommand(FTPCommand command) throws IOException {
		synchronized (queueSync) {
			return queueClosableCommandInner(command);
		}
	}
	
	/**
	 * Waits for a connection to be set and then returns its
	 * <code>InputStream</code>.
	 * @return the client connection stream
	 */
	protected abstract InputStream getInputStream() throws IOException;
	
	/**
	 * Waits for a connection to be set and then returns its
	 * <code>OutputStream</code>.
	 * @return the client connection stream
	 */
	protected abstract OutputStream getOutputStream() throws IOException;
	protected abstract boolean isStopRequested();
	
	@Override
	public void quit() throws IOException {
		queueCommand(new FTPCommandQuit());
	}


	@Override
	public void setActiveMode() throws IOException {
		getState().modeCommand = new FTPCommandActive();
	}

	@Override
	public void setPassiveMode() throws IOException {
		getState().modeCommand = new FTPCommandPassive();
	}

	@Override
	public FTPFutureData<FTPFile> getFile(FTPFilename file) throws IOException {
		return queueDataCommand(FTPTransformation.FILE_TRANSFORMATION, new FTPCommandFileRetrieve(file), file);
	}

	@Override
	public FTPFuture writeFile(FTPFilename file, FTPFile data) throws IOException {
		return queueFileCommand(new FTPCommandFile(FileAction.WRITE, file, data), file);
	}

	@Override
	public FTPFuture appendFile(FTPFilename file, FTPFile data) throws IOException {
		return queueFileCommand(new FTPCommandFile(FileAction.APPEND, file, data), file);
	}

	@Override
	public FTPFuture deleteFile(FTPFilename file) throws IOException {
		return queueCommand(new FTPCommandFileDelete(file));
	}

	@Override
	public FTPFutureData<FTPEntity[]> getFileList(FTPFilename directory) throws IOException {
		return queueDataCommand(FTPTransformation.FILELIST_TRANSFORMATION, new FTPCommandList(directory));
	}

	@Override
	public FTPFutureData<String> getWorkingDirectory(FTPFilename directory) throws IOException {
		return queueDataCommand(FTPTransformation.ASCII_TRANSFORMATION, new FTPCommandDirectory());
	}

	@Override
	public FTPFuture changeWorkingDirectory(FTPFilename directory) throws IOException {
		return queueCommand(new FTPCommandChained(false, new FTPCommandDirectory(DirectoryAction.CHANGE, directory), new FTPCommandDirectory()));
	}

	@Override
	public FTPFuture makeDirectory(FTPFilename directory) throws IOException {
		return queueCommand(new FTPCommandChained(false, new FTPCommandDirectory(DirectoryAction.MAKE, directory), new FTPCommandDirectory()));
	}

	@Override
	public FTPFuture deleteDirectory(FTPFilename directory) throws IOException {
		return queueCommand(new FTPCommandChained(false, new FTPCommandDirectory(DirectoryAction.REMOVE, directory), new FTPCommandDirectory()));
	}

	@Override
	public FTPFuture completelyDeleteDirectory(FTPFilename directory) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> FTPFutureData<T> queueDataCommand(FTPTransformation<T> function, FTPCommand command) throws IOException {
		return queueDataCommand(function, command, null);
	}

	@Override
	public <T> FTPFutureData<T> queueDataCommand(final FTPTransformation<T> function, FTPCommand command, FTPFilename file) throws IOException {
		return new FTPFutureData<T>(queueFileCommand(command, file)) {
			@Override
			protected T formData(byte[] result) throws Exception {
				return function.transform(getState(), result);
			}
		};
	}

	@Override
	public FTPFuture queueFileCommand(FTPCommand command) throws IOException {
		return queueFileCommand(command, null);
	}

	@Override
	public FTPFuture queueFileCommand(FTPCommand command, FTPFilename file) throws IOException {
		return queueCommand(new FTPCommandChained(new FTPCommand[] { new FTPCommandType(getFTPType(file)), getState().modeCommand, command }));
	}

	@Override
	public FTPFilename getAbsoluteFilename(String file) {
		return new FTPFilename("/", file);
	}

	@Override
	public FTPFilename getRelativeFilename(String file) {
		return new FTPFilename(getState().workingDirectory, file);
	}
	
	/**
	 * Retrieves the type character for the specified file.
	 * @param file the file to test
	 * @return the type char
	 * @see FTPTypeDecider
	 * @since 0.6a
	 */
	private char getFTPType(FTPFilename file) {
		if (file == null) return FTPTypeDecider.ASCII;
		
		String filename = file.getName();
		int index = filename.lastIndexOf('.');
		if (index > -1) return FTPTypeDecider.decideFTPType(filename.substring(index + 1), index != 0);
		return FTPTypeDecider.decideFTPType("", !filename.isEmpty());
	}
}
