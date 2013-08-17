package com.hsun324.ftplite.client;

import java.io.IOException;
import java.io.OutputStream;

import com.hsun324.ftplite.FTPCommand;
import com.hsun324.ftplite.FTPEntity;
import com.hsun324.ftplite.FTPFile;
import com.hsun324.ftplite.FTPFilename;
import com.hsun324.ftplite.FTPFuture;
import com.hsun324.ftplite.FTPFutureData;
import com.hsun324.ftplite.FTPTransformation;
/**
 * This interface represents a typical File Transfer Protocol (FTP)
 * client as defined by RFC 959. This class provides methods to
 * manipulate files and folders on remote systems using FTP.
 * <p>
 * The <code>FTPClient</code> is synchronized over multiple threads. Commands sent
 * to the server are not executed immediately, but instead are
 * represented by either {@link FTPFuture} or {@link FTPFutureData} objects with
 * blocking result retrieval methods.
 * 
 * @author hsun324
 * @version 0.7
 * @since 0.7
 */
public interface FTPClient {
	/**
	 * Gets the <code>OutputStream</code> that is used to send commands to the connected server.
	 * @return the control line output stream
	 */
	public OutputStream getCommandStream();
	
	/**
	 * Adds a command to the command queue for later execution and returns an {@link FTPFuture} element representing
	 * the progress of the execution of the current command.
	 * @param command the command to queue
	 * @return a ftp future representing the command
	 * @throws IOException
	 */
	public FTPFuture queueCommand(FTPCommand command) throws IOException;
	
	/**
	 * If not yet connected to the server, this method causes this <code>FTPClient</code> connect
	 * to the server using the provided user and password. If already connected, this method will
	 * issue a reinitialize command and login using the provided user and password.
	 * <p>
	 * The <code>FTPClient</code> may not immediately connect to server and be ready for commands. This
	 * method returns a {@link FTPFuture} that will recieve a result when the client is connected and
	 * ready.
	 * <p>
	 * If the password is <code>null</code> then this method will assume that the user requires no
	 * password and will cause the connection to fail if one is required.
	 * @param user the user to connect as
	 * @param password the password to use
	 * @return a ftp future representing the connection state
	 * @throws IOException
	 */
	public FTPFuture connect(String user, String password) throws IOException;
	
	/**
	 * Queues a command for the <code>FTPClient</code> to disconnect.
	 * @throws IOException
	 */
	public void quit() throws IOException;
	
	/**
	 * Closes the current connection and frees related resources.
	 * @throws IOException
	 */
	public void close();

	/**
	 * Sets the current transmission stream mode to active which
	 * means that the server must start the data connection.
	 * @throws IOException
	 */
	public void setActiveMode() throws IOException;
	
	/**
	 * Sets the current transmission stream mode to passive which
	 * means that the client must start the data connection.
	 * @throws IOException
	 */
	public void setPassiveMode() throws IOException;

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
	public FTPFutureData<FTPFile> getFile(FTPFilename file) throws IOException;
	
	/**
	 * Queues a data command to write to a file denoted by the file.
	 * <p>
	 * This method requires a FTPFile with the data that is to
	 * be written to the file.
	 * @param file the file to write to
	 * @param data the data to write to the file
	 * @return a ftp future representing the command
	 * @throws IOException
	 * @since 0.6a
	 */
	public FTPFuture writeFile(FTPFilename file, FTPFile data) throws IOException;

	/**
	 * Queues a data command to append to a file denoted by the file.
	 * <p>
	 * This method requires a FTPFile with the data that is to
	 * be appended to the file.
	 * @param file the file to append to
	 * @param data the data to append to the file
	 * @return a ftp future representing the command
	 * @throws IOException
	 * @since 0.6a
	 */
	public FTPFuture appendFile(FTPFilename file, FTPFile data) throws IOException;
	
	/**
	 * Queues a command to delete the file denoted by the file.
	 * @param file the file to delete
	 * @return a ftp future representing the command
	 * @throws IOException
	 * @since 0.6a
	 */
	public FTPFuture deleteFile(FTPFilename file) throws IOException;
	
	/**
	 * Queues a data command to retrieve a list of the objects within
	 * the directory denoted by the directory.
	 * <p>
	 * This method returns a {@link FTPFutureData} object of the
	 * generic {@link FTPEntity}<code>[]</code> which represents the command and the
	 * list being retrieved.
	 * @param file the directory to list
	 * @return a ftp future representing the command
	 * @throws IOException
	 * @since 0.5
	 */
	public FTPFutureData<FTPEntity[]> getFileList(FTPFilename directory) throws IOException;

	/**
	 * Queues a data command to retrieve the current directory
	 * <p>
	 * This method returns a {@link FTPFutureData} object of the
	 * generic <code>String</code> which represents the command and the
	 * current directory being retrieved.
	 * @param file the directory to list
	 * @return a ftp future representing the command
	 * @throws IOException
	 * @since 0.6a
	 */
	public FTPFutureData<String> getWorkingDirectory(FTPFilename directory) throws IOException;
	
	/**
	 * Queues a command to change the current directory to the one indicated
	 * by directory.
	 * @param directory the directory to change to
	 * @return a ftp future representing the command
	 * @throws IOException
	 * @since 0.5
	 */
	public FTPFuture changeWorkingDirectory(FTPFilename directory) throws IOException;
	
	/**
	 * Queues a command to create the directory indicated by directory.
	 * @param directory the directory to create
	 * @return a ftp future representing the command
	 * @throws IOException
	 * @since 0.6a
	 */
	public FTPFuture makeDirectory(FTPFilename directory) throws IOException;
	
	/**
	 * Queues a command to delete a directory indicated by directory.
	 * <p>
	 * The directory indicated to be deleted must be completely empty
	 * (on most server implementations) to be able to be successfully
	 * deleted.
	 * @param directory the directory to delete
	 * @return a ftp future representing the command
	 * @throws IOException
	 * @since 0.6a
	 */
	public FTPFuture deleteDirectory(FTPFilename directory) throws IOException;

	/**
	 * Attempts to completely delete the directory specified by directory.
	 * <p>
	 * This method is currently unimplemented.
	 * @param directory the directory to delete
	 * @return a ftp future representing the command
	 * @throws IOException
	 * @since 0.6a
	 */
	public FTPFuture completelyDeleteDirectory(FTPFilename directory) throws IOException;
	
	/**
	 * Queues a data command with the specified {@link FTPTransformation} function.
	 * <p>
	 * The function will be used to convert the generic <code>byte[]</code> result
	 * of {@link FTPDownloadCommand}s to a more usable form.
	 * @param function the transformation to use
	 * @param command the command to queue
	 * @return a ftp future representing the data command
	 * @throws IOException
	 * @since 0.5
	 */
	public <T> FTPFutureData<T> queueDataCommand(final FTPTransformation<T> function, FTPCommand command) throws IOException;
	
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
	 * @since 0.5
	 */
	public <T> FTPFutureData<T> queueDataCommand(final FTPTransformation<T> function, FTPCommand command, FTPFilename file) throws IOException;

	/**
	 * Queues a file command.
	 * @param command the command to queue
	 * @return a ftp future representing the file command
	 * @throws IOException
	 * @since 0.6a
	 */
	public FTPFuture queueFileCommand(FTPCommand command) throws IOException;
	/**
	 * Queues a file command upon the specified file.
	 * @param command the command to queue
	 * @param file the target file
	 * @return a ftp future representing the file command
	 * @throws IOException
	 * @since 0.6a
	 */
	public FTPFuture queueFileCommand(FTPCommand command, FTPFilename file) throws IOException;

	/**
	 * Constructs a new absolute {@link FTPFilename} with the path
	 * specified by file.
	 * @param file the path
	 * @return a filename representing the path
	 * @since 0.6a
	 */
	public FTPFilename getAbsoluteFilename(String file);
	
	/**
	 * Constructs a new {@link FTPFilename} with the path
	 * specified by file that is relative to the current
	 * directory of the <code>FTPClient</code>.
	 * @param file the path
	 * @return a filename representing the relative path
	 * @since 0.6a
	 */
	public FTPFilename getRelativeFilename(String file);
}
