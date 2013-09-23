package com.hsun324.ftp.ftplite.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.hsun324.ftp.ftplite.FTPFile;
import com.hsun324.ftp.ftplite.FTPFuture;
import com.hsun324.ftp.ftplite.FTPFutureData;
import com.hsun324.ftp.ftplite.FTPObject;
import com.hsun324.ftp.ftplite.FTPTransformation;
import com.hsun324.ftp.ftplite.FTPTypeDecider;
import com.hsun324.ftp.ftplite.client.FTPInterface.ClientState;
import com.hsun324.ftp.ftplite.commands.*;
import com.hsun324.ftp.ftplite.commands.TextCommandDirectory.DirectoryAction;
import com.hsun324.ftp.ftplite.commands.UploadCommandFile.FileAction;

/**
 * This abstract class represents a typical File Transfer Protocol
 * (FTP) client as defined by RFC 959. This class provides methods
 * to manipulate files and folders on remote systems using FTP.
 * <p>
 * The <code>FTPClient</code> is synchronized over multiple threads.
 * Commands sent to the server are not executed immediately, but
 * instead are represented by either {@link FTPFuture} or
 * {@link FTPFutureData} objects with blocking result retrieval
 * methods.
 * 
 * @author hsun324
 * @version 0.7
 */
public abstract class FTPClient {
	protected abstract FTPInterface getInterface();
	
	/**
	 * Adds a command to the command queue for later execution and returns an {@link FTPFuture} element representing
	 * the progress of the execution of the current command.
	 * @param command the command to queue
	 * @return a ftp future representing the command
	 * @throws IOException
	 */
	public FTPFuture queueCommand(Command command) throws IOException {
		if (!getInterface().isConnected()) throw new IllegalStateException("conn closed");
		return queueCommandUnsafe(command);
	}
	
	/**
	 * Adds a command to the command queue for later execution and returns an {@link FTPFuture} element representing
	 * the progress of the execution of the current command.
	 * <p>
	 * This method does not check the status of the connection or queue thread before adding the command to the
	 * queue.
	 * @param command the command to queue
	 * @return a ftp future representing the command
	 * @throws IOException
	 */
	protected abstract FTPFuture queueCommandUnsafe(Command command) throws IOException;
	
	/**
	 * If not yet connected to the server, this method causes this <code>FTPClient</code> connect
	 * to the server using the provided user and password. If already connected, this method will
	 * issue a reinitialize command and login using the provided user and password.
	 * <p>
	 * The <code>FTPClient</code> may not immediately connect to server and be ready for commands. This
	 * method returns a {@link FTPFuture} that will receive a result when the client is connected and
	 * ready.
	 * <p>
	 * If the password is <code>null</code> then this method will assume that the user requires no
	 * password and will cause the connection to fail if one is required.
	 * @param user the user to connect as
	 * @param password the password to use
	 * @return a ftp future representing the connection state
	 * @throws IOException
	 */
	public FTPFuture connect(String user, String password) throws IOException {
		Command firstCommand = null;
		
		if (!getInterface().isConnected()) {
			beginConnection();
			firstCommand = new TextCommandConnect();
		} else firstCommand = new TextCommandReinitialize();
		
		return queueCommandUnsafe(new ChainedCommand(new Command[]{
			firstCommand,
			new TextCommandUser(user),
			new TextCommandPassword(password),
			new TextCommandSystem(),
			new TextCommandFeatures(),
			new TextCommandDirectory()
		}));
	}
	
	protected abstract void beginConnection();
	
	/**
	 * Queues a command for the <code>FTPClient</code> to disconnect.
	 * @throws IOException
	 */
	public void quit() throws IOException {
		queueCommand(new TextCommandQuit());
	}
	
	/**
	 * Closes the current connection and frees related resources.
	 * @throws IOException
	 */
	public void close() throws IOException {
		FTPInterface inter = getInterface();
		
		inter.setClientState(ClientState.CLOSING);
		closeConnection();
		inter.setClientState(ClientState.CLOSED);
	}
	
	protected abstract void closeConnection() throws IOException;
	
	protected abstract InputStream getInputStream() throws IOException;
	protected abstract OutputStream getOutputStream() throws IOException;

	/**
	 * Sets the current transmission stream mode to active which
	 * means that the server must start the data connection.
	 * @throws IOException
	 */
	public void setActiveMode() throws IOException {
		getInterface().setModeCommand(new ModeCommandActive());
	}
	
	/**
	 * Sets the current transmission stream mode to passive which
	 * means that the client must start the data connection.
	 * @throws IOException
	 */
	public void setPassiveMode() throws IOException {
		getInterface().setModeCommand(new ModeCommandPassive());
	}

	/**
	 * Queues a data command to retrieve a file denoted by the file.
	 * <p>
	 * This method returns a {@link FTPFutureData} object of the
	 * generic {@link FTPFile} which represents the command and the
	 * data contained in the file being retrieved.
	 * @param file the file to retreive
	 * @return a ftp future representing the command
	 * @throws IOException
	 */
	public FTPFutureData<FTPFile> getFile(FTPObject file) throws IOException {
		return queueDataCommand(FTPTransformation.FILE_TRANSFORMATION, new DownloadCommandRetrieveFile(file), file);
	}
	
	/**
	 * Queues a data command to write to a file denoted by the file.
	 * <p>
	 * This method requires a FTPFile with the data that is to
	 * be written to the file.
	 * @param file the file to write to
	 * @param data the data to write to the file
	 * @return a ftp future representing the command
	 * @throws IOException
	 */
	public FTPFuture writeFile(FTPObject file, FTPFile data) throws IOException {
		return queueFileCommand(new UploadCommandFile(FileAction.WRITE, file, data), file);
	}

	/**
	 * Queues a data command to append to a file denoted by the file.
	 * <p>
	 * This method requires a FTPFile with the data that is to
	 * be appended to the file.
	 * @param file the file to append to
	 * @param data the data to append to the file
	 * @return a ftp future representing the command
	 * @throws IOException
	 */
	public FTPFuture appendFile(FTPObject file, FTPFile data) throws IOException {
		return queueFileCommand(new UploadCommandFile(FileAction.APPEND, file, data), file);
	}
	
	/**
	 * Queues a command to delete the file denoted by the file.
	 * @param file the file to delete
	 * @return a ftp future representing the command
	 * @throws IOException
	 */
	public FTPFuture deleteFile(FTPObject file) throws IOException {
		return queueCommand(new TextCommandDeleteFile(file));
	}
	
	/**
	 * Queues a data command to retrieve a list of the objects within
	 * the directory denoted by the directory.
	 * <p>
	 * This method returns a {@link FTPFutureData} object of the
	 * generic {@link FTPEntity}<code>[]</code> which represents the command and the
	 * list being retrieved.
	 * @param directory the directory to list
	 * @return a ftp future representing the command
	 * @throws IOException
	 */
	public FTPFutureData<FTPObject[]> getFileList(FTPObject directory) throws IOException {
		return queueDataCommand(FTPTransformation.FILELIST_TRANSFORMATION, new DownloadCommandList(directory));
	}

	/**
	 * Queues a data command to retrieve the current directory
	 * <p>
	 * This method returns a {@link FTPFutureData} object of the
	 * generic <code>String</code> which represents the command and the
	 * current directory being retrieved.
	 * @param directory the directory to list
	 * @return a ftp future representing the command
	 * @throws IOException
	 */
	public FTPFutureData<String> getWorkingDirectory(FTPObject directory) throws IOException {
		return queueDataCommand(FTPTransformation.ASCII_TRANSFORMATION, new TextCommandDirectory());
	}
	
	/**
	 * Queues a command to change the current directory to the one indicated
	 * by directory.
	 * @param directory the directory to change to
	 * @return a ftp future representing the command
	 * @throws IOException
	 */
	public FTPFuture changeWorkingDirectory(FTPObject directory) throws IOException {
		return queueCommand(new ChainedCommand(false, new TextCommandDirectory(DirectoryAction.CHANGE, directory), new TextCommandDirectory()));
	}

	/**
	 * Queues a command to create the directory indicated by directory.
	 * @param directory the directory to create
	 * @return a ftp future representing the command
	 * @throws IOException
	 */
	public FTPFuture makeDirectory(FTPObject directory) throws IOException {
		return queueCommand(new ChainedCommand(false, new TextCommandDirectory(DirectoryAction.MAKE, directory), new TextCommandDirectory()));
	}
	
	/**
	 * Queues a command to delete a directory indicated by directory.
	 * <p>
	 * The directory indicated to be deleted must be completely empty
	 * (on most server implementations) to be able to be successfully
	 * deleted.
	 * @param directory the directory to delete
	 * @return a ftp future representing the command
	 * @throws IOException
	 */
	public FTPFuture deleteDirectory(FTPObject directory) throws IOException {
		return queueCommand(new ChainedCommand(false, new TextCommandDirectory(DirectoryAction.REMOVE, directory), new TextCommandDirectory()));
	}

	/**
	 * Attempts to completely delete the directory specified by directory.
	 * <p>
	 * This method is currently unimplemented.
	 * @param directory the directory to delete
	 * @return a ftp future representing the command
	 * @throws IOException
	 */
	public abstract FTPFuture completelyDeleteDirectory(FTPObject directory) throws IOException;
	
	/**
	 * Queues a data command with the specified {@link FTPTransformation} function.
	 * <p>
	 * The function will be used to convert the generic <code>byte[]</code> result
	 * of {@link FTPDownloadCommand}s to a more usable form.
	 * @param function the transformation to use
	 * @param command the command to queue
	 * @return a ftp future representing the data command
	 * @throws IOException
	 */
	public <T> FTPFutureData<T> queueDataCommand(final FTPTransformation<T> function, Command command) throws IOException {
		return queueDataCommand(function, command, null);
	}
	
	/**
	 * Queues a data command with the specified {@link FTPTransformation} function
	 * that will be applied to the specified file.
	 * <p>
	 * The function will be used to convert the generic <code>byte[]</code> result
	 * of {@link FTPDownloadCommand}s to a more usable form.
	 * @param function the transformation to use
	 * @param command the command to queue
	 * @param file the target file
	 * @return a ftp future representing the data command
	 * @throws IOException
	 */
	public <T> FTPFutureData<T> queueDataCommand(final FTPTransformation<T> function, Command command, FTPObject file) throws IOException {
		return new FTPFutureData<T>(queueFileCommand(command, file)) {
			@Override
			protected T formData(byte[] result) throws Exception {
				return function.transform(getInterface(), result);
			}
		};
	}


	/**
	 * Queues a file command.
	 * @param command the command to queue
	 * @return a ftp future representing the file command
	 * @throws IOException
	 */
	public FTPFuture queueFileCommand(Command command) throws IOException {
		return queueFileCommand(command, null);
	}
	
	/**
	 * Queues a file command upon the specified file.
	 * @param command the command to queue
	 * @param file the target file
	 * @return a ftp future representing the file command
	 * @throws IOException
	 */
	public FTPFuture queueFileCommand(Command command, FTPObject file) throws IOException {
		return queueCommand(new ChainedCommand(new Command[] { new TextCommandType(getFTPType(file)), getInterface().getModeCommand(), command }));
	}

	/**
	 * Constructs a new absolute {@link FTPFilename} with the path
	 * specified by file.
	 * @param file the path
	 * @return a filename representing the path
	 */
	public FTPObject getAbsoluteFilename(String file) {
		return new FTPObject("/", file);
	}
	
	/**
	 * Constructs a new {@link FTPFilename} with the path
	 * specified by file that is relative to the current
	 * directory of the <code>FTPClient</code>.
	 * @param file the path
	 * @return a filename representing the relative path
	 */
	public FTPObject getRelativeFilename(String file) {
		return new FTPObject(getInterface().getCurrentDirectory(), file);
	}
	
	/**
	 * Retrieves the type character for the specified file.
	 * @param file the file to test
	 * @return the type char
	 * @see FTPTypeDecider
	 */
	protected char getFTPType(FTPObject file) {
		if (file == null) return FTPTypeDecider.ASCII;
		
		String filename = file.getName();
		int index = filename.lastIndexOf('.');
		if (index > -1) return FTPTypeDecider.decideFTPType(filename.substring(index + 1), index != 0);
		return FTPTypeDecider.decideFTPType("", !filename.isEmpty());
	}

	protected abstract String getUniqueString();
}
