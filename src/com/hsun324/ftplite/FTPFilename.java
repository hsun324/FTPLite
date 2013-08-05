package com.hsun324.ftplite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FTPFilename {
	private static final String[] STRING_PROTOTYPE_ARRAY = new String[0];
	
	public static final FTPFilename CURRENT_DIRECTORY = new FTPFilename("");
	public static final FTPFilename ROOT_DIRECTORY = new FTPFilename("/");
	
	protected final boolean absolute;
	protected final String[] tokens;
	
	public FTPFilename(String path) {
		this.absolute = path.startsWith("/");
		this.tokens = addValidTokens(new ArrayList<String>(), path).toArray(STRING_PROTOTYPE_ARRAY);
	}
	public FTPFilename(FTPFilename parent, String path) {
		boolean pathAbsolute = path.startsWith("/");
		
		this.tokens = addValidTokens(pathAbsolute ? new ArrayList<String>() : new ArrayList<String>(Arrays.asList(parent.tokens)), path).toArray(STRING_PROTOTYPE_ARRAY);
		this.absolute = pathAbsolute || parent.absolute;
	}
	public FTPFilename(FTPFilename parent, FTPFilename path) {
		if (path.absolute) this.tokens = Arrays.copyOf(path.tokens, path.tokens.length);
		else this.tokens = joinArrays(parent.tokens, path.tokens);
		
		this.absolute = parent.absolute || path.absolute;
	}
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
	protected String path = null;
	public String getPath() {
		if (path == null) this.path = (absolute ? "/" : "") + joinTokens(this.tokens, "/");
		return path;
	}
	protected String name = null;
	public String getName() {
		if (name == null) this.name = (tokens.length == 0 ? "" : tokens[tokens.length - 1]);
		return this.name;
	}
	
	private String joinTokens(String[] tokens, String delim) {
		if (tokens.length == 0) return "";
		if (tokens.length == 1) return tokens[0];
		
		StringBuilder builder = new StringBuilder().append(tokens[0]);
		int length = tokens.length;
		for (int i = 1; i < length; i++) builder.append(delim).append(tokens[i]);
		
		return builder.toString();
	}
	private String[] joinArrays(String[] x, String[] y) {
		   int lengthX = x.length, lengthY = y.length;
		   String[] sum = new String[lengthX + lengthY];
		   System.arraycopy(x, 0, y, 0, lengthX);
		   System.arraycopy(x, 0, y, lengthX, lengthY);
		   return sum;
	}
	private List<String> addValidTokens(List<String> list, String path) {
		for (String token : path.split("\\\\|/")) if (!token.isEmpty()) list.add(token);
		return list;
	}
}
