package com.hsun324.ftplite;

import java.util.ArrayList;
import java.util.List;

public class FTPFilename {
	private static final String[] STRING_PROTOTYPE_ARRAY = new String[0];

	public static final FTPFilename CURRENT_DIRECTORY = new FTPFilename("", "");
	public static final FTPFilename ROOT_DIRECTORY = new FTPFilename("/");
	
	private static String joinTokens(String[] tokens, String delim) {
		if (tokens.length == 0) return "";
		if (tokens.length == 1) return tokens[0];
		
		StringBuilder builder = new StringBuilder().append(tokens[0]);
		int length = tokens.length;
		for (int i = 1; i < length; i++)
			builder.append(delim).append(tokens[i]);
		
		return builder.toString();
	}
	
	protected final String path;
	protected final boolean leadingSlash;
	protected final String[] tokens;
	public FTPFilename(String path) {
		this(CURRENT_DIRECTORY, path);
	}
	public FTPFilename(FTPFilename parent, String path) {
		this(parent.toString(), path);
	}
	public FTPFilename(String parent, String path) {
		boolean parentStartsSlash = parent.trim().startsWith("/");
		boolean pathStartsSlash = path.trim().startsWith("/");
		String concatPath = (!pathStartsSlash ? parent + "/" : "") + path;
		
		List<String> tokenList = new ArrayList<String>();
		for (String token : concatPath.split("/")) if (!token.isEmpty()) tokenList.add(token);
		
		this.tokens = tokenList.toArray(STRING_PROTOTYPE_ARRAY);
		this.leadingSlash = parentStartsSlash || pathStartsSlash;
		this.path = (this.leadingSlash ? "/" : "") + joinTokens(this.tokens, "/");
	}
	
	@Override
	public String toString() {
		return path;
	}
}
