package com.hsun324.ftp.ftplite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A class that represents a virtual filename pointing to any entity.
 * <p>
 * This class is mainly used to abstract away file system and current directory
 * ambiguities.
 * @author hsun324
 * @version 0.6a
 * @since 0.5
 */
public class FTPFilename {
	/**
	 * A prototype array of <code>String</code>.
	 */
	private static final String[] STRING_PROTOTYPE_ARRAY = new String[0];
	
	/**
	 * A filename pointing the current directory.
	 */
	public static final FTPFilename CURRENT_DIRECTORY = new FTPFilename("");
	/**
	 * A filename pointing the root directory.
	 */
	public static final FTPFilename ROOT_DIRECTORY = new FTPFilename("/");
	
	/**
	 * Flag indicating whether this path is absolute.
	 */
	protected final boolean absolute;
	/**
	 * The list of tokens in this filename.
	 */
	protected final String[] tokens;
	
	/**
	 * Creates a <code>FTPFilename</code> from the specified path.
	 * @param path the path
	 */
	public FTPFilename(String path) {
		this.absolute = path.startsWith("/");
		this.tokens = addValidTokens(new ArrayList<String>(), path).toArray(STRING_PROTOTYPE_ARRAY);
	}
	/**
	 * Creates a <code>FTPFilename</code> from the specified path
	 * with the specified parent.
	 * <p>
	 * If the path is absolute (starts with a /) then the parent
	 * will be ignored.
	 * @param parent the parent
	 * @param path the path
	 */
	public FTPFilename(FTPFilename parent, String path) {
		boolean pathAbsolute = path.startsWith("/");
		
		this.tokens = addValidTokens(pathAbsolute ? new ArrayList<String>() : new ArrayList<String>(Arrays.asList(parent.tokens)), path).toArray(STRING_PROTOTYPE_ARRAY);
		this.absolute = pathAbsolute || parent.absolute;
	}
	/**
	 * Creates a <code>FTPFilename</code> from the specified path
	 * with the specified parent.
	 * <p>
	 * If the path is absolute then the parent
	 * will be ignored.
	 * @param parent the parent
	 * @param path the path
	 */
	public FTPFilename(FTPFilename parent, FTPFilename path) {
		if (path.absolute) this.tokens = Arrays.copyOf(path.tokens, path.tokens.length);
		else this.tokens = joinArrays(parent.tokens, path.tokens);
		
		this.absolute = parent.absolute || path.absolute;
	}
	/**
	 * Creates a <code>FTPFilename</code> from the specified path
	 * with the specified parent.
	 * <p>
	 * If the path is absolute (starts with a /) then the parent
	 * will be ignored.
	 * @param parent the parent
	 * @param path the path
	 */
	public FTPFilename(String parent, String path) {
		boolean parentAbsolute = parent.startsWith("/");
		boolean pathAbsolute = path.startsWith("/");
		
		String combinedPath = (pathAbsolute ? "" : parent + "/") + path;

		this.tokens = addValidTokens(new ArrayList<String>(), combinedPath).toArray(STRING_PROTOTYPE_ARRAY);
		this.absolute = pathAbsolute || parentAbsolute;
	}
	
	@Override
	public String toString() {
		return getPath();
	}
	/**
	 * Path cache variable.
	 */
	protected String path = null;
	/**
	 * Gets a fully qualified path for this filename.
	 * @return filename path
	 */
	public String getPath() {
		if (path == null) this.path = (absolute ? "/" : "") + joinTokens(this.tokens, "/");
		return path;
	}
	/**
	 * Name cache variable.
	 */
	protected String name = null;
	/**
	 * Gets the filename for this <code>FTPFilename</code>.
	 * @return filename name
	 */
	public String getName() {
		if (name == null) this.name = (tokens.length == 0 ? "" : tokens[tokens.length - 1]);
		return this.name;
	}
	
	/**
	 * Helper function that joins tokens with the provided delimiter.
	 * @param tokens the tokens
	 * @param delim the delimiter
	 * @return the joined tokens
	 */
	private String joinTokens(String[] tokens, String delim) {
		if (tokens.length == 0) return "";
		if (tokens.length == 1) return tokens[0];
		
		StringBuilder builder = new StringBuilder().append(tokens[0]);
		int length = tokens.length;
		for (int i = 1; i < length; i++) builder.append(delim).append(tokens[i]);
		
		return builder.toString();
	}
	/**
	 * Helper function that joins arrays.
	 * @param x an array
	 * @param y another array
	 * @return the joined arrays
	 */
	private String[] joinArrays(String[] x, String[] y) {
		   int lengthX = x.length, lengthY = y.length;
		   String[] sum = new String[lengthX + lengthY];
		   System.arraycopy(x, 0, y, 0, lengthX);
		   System.arraycopy(x, 0, y, lengthX, lengthY);
		   return sum;
	}
	/**
	 * Helper function that adds valid tokens split
	 * from the path to the list.
	 * <p>
	 * This function returns the list it was given for
	 * convenience.
	 * @param list the list
	 * @param path the path
	 * @return the provided list
	 */
	private List<String> addValidTokens(List<String> list, String path) {
		for (String token : path.split("\\\\|/")) if (!token.isEmpty()) list.add(token);
		return list;
	}
}
