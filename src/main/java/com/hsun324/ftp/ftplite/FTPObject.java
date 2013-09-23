package com.hsun324.ftp.ftplite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FTPObject {
	private static final String[] STRING_PROTOTYPE_ARRAY = new String[0];
	
	public static final FTPObject ROOT_DIRECTORY = new FTPObject(FTPObjectType.DIRECTORY, "/");
	public static final FTPObject CURRENT_DIRECTORY = new FTPObject(FTPObjectType.DIRECTORY, ".");
	
	private static final Pattern BASIC_ENTRY_PATTERN = Pattern.compile("([-drwx]{10})\\s+(?:[^\\s]+\\s+){6,7}(.+)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	private static final Pattern META_ENTRY_PATTERN = Pattern.compile("(?:((?:[^;]+=[^;]+;)*) )?([^;]+)$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	
	/**
	 * Parses a single line entity descriptor using the entry patterns.
	 * @param currentDirectory the current client directory
	 * @param line the directory line
	 * @return an entity representing the line
	 */
	public static FTPObject parseEntity(FTPObject currentDirectory, String line) {
		try {
			Matcher matcher = BASIC_ENTRY_PATTERN.matcher(line);
			if (matcher.find()) {
				if (matcher.group(1).toLowerCase().contains("d")) return new FTPObject(FTPObjectType.DIRECTORY, currentDirectory, matcher.group(2));
				else return new FTPObject(FTPObjectType.FILE, currentDirectory, matcher.group(2));
			} else {
				matcher = META_ENTRY_PATTERN.matcher(line);
				if (matcher.find()) {
					FTPObjectType type = FTPObjectType.FILE;
					Date modified = new Date(0);
					int size = -1;
	
					String[] split = matcher.group(1).split(";");
					for(String fact : split) {
						String[] factPair = fact.split("=");
						if (factPair.length == 2) {
							if (factPair[0].equalsIgnoreCase("type")) {
								if (factPair[1].equalsIgnoreCase("dir")) type = FTPObjectType.DIRECTORY;
								else if (!factPair[1].equalsIgnoreCase("file")) return null;
							} else if (factPair[0].equalsIgnoreCase("size")) {
								size = i(factPair[1]);
							} else if (factPair[0].equalsIgnoreCase("modified")) {
								modified = getDate(factPair[1]);
							}
						}
					}
					FTPObject object = new FTPObject(type, currentDirectory, matcher.group(2));
					object.setModifiedTime(modified);
					object.setSize(size);
					return object;
				}
			}
		} catch (Exception e) { }
		return null;
	}
	
	private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{4})(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	private static Date getDate(String date) {
		Matcher matcher = DATE_PATTERN.matcher(date);
		if (matcher.find()) {
			Calendar calendar = Calendar.getInstance();
			calendar.set(i(matcher.group(1)), i(matcher.group(2)), i(matcher.group(3)),
					i(matcher.group(4)), i(matcher.group(5)), i(matcher.group(63)));
			return calendar.getTime();
		}
		return null;
	}
	private static int i(String s) {
		return Integer.parseInt(s);
	}

	
	private final FTPObjectType type;
	
	private final boolean pathAbsolute;
	private final String[] pathTokens;
	
	private boolean modifiedTimeSet = false;
	private Date modifiedTime = null;
	
	private boolean sizeSet = false;
	private long size = 0;
	
	public FTPObject(String path) {
		this(FTPObjectType.UNKNOWN, path);
	}
	
	public FTPObject(FTPObjectType type, String path) {
		this(type, "", path);
	}

	public FTPObject(FTPObject parent, String path) {
		this(FTPObjectType.UNKNOWN, parent, path);
	}
	
	public FTPObject(String parent, String path) {
		this(FTPObjectType.UNKNOWN, parent, path);
	}
	
	public FTPObject(FTPObjectType type, String parent, String path) {
		this.type = type;
		
		boolean pathAbsolute = path.startsWith("/");
		String pathString = (pathAbsolute ? path : parent + "/" + path).replace('\\', '/');
		
		this.pathTokens = addValidTokens(new ArrayList<String>(), pathString).toArray(STRING_PROTOTYPE_ARRAY);
		this.pathAbsolute = pathAbsolute || parent.startsWith("/");
	}
	
	public FTPObject(FTPObjectType type, FTPObject parent, String path) {
		this.type = type;
		
		boolean pathAbsolute = path.startsWith("/");
		
		this.pathTokens = addValidTokens(pathAbsolute ? new ArrayList<String>() : new ArrayList<String>(Arrays.asList(parent.pathTokens)), path).toArray(STRING_PROTOTYPE_ARRAY);
		this.pathAbsolute = pathAbsolute || parent.pathAbsolute;
	}
	
	public FTPObject(FTPObject parent, FTPObject path) {
		this(FTPObjectType.UNKNOWN, parent, path);
	}
	
	public FTPObject(FTPObjectType type, FTPObject parent, FTPObject path) {
		this.type = type;
		
		if (path.pathAbsolute) this.pathTokens = Arrays.copyOf(path.pathTokens, path.pathTokens.length);
		else this.pathTokens = joinArrays(parent.pathTokens, path.pathTokens);
		
		this.pathAbsolute = parent.pathAbsolute || path.pathAbsolute;
	}
	
	public FTPObjectType getType() {
		return type;
	}
	
	private String path = null;
	/**
	 * Gets a fully qualified path for this filename.
	 * @return filename path
	 */
	public String getPath() {
		if (path == null) this.path = (pathAbsolute ? "/" : "") + joinTokens(this.pathTokens, "/");
		return path;
	}
	
	private String name = null;
	/**
	 * Gets the filename for this <code>FTPFilename</code>.
	 * @return filename name
	 */
	public String getName() {
		if (name == null) this.name = (pathTokens.length == 0 ? "" : pathTokens[pathTokens.length - 1]);
		return this.name;
	}
	
	public boolean isModifiedTimeSet() {
		return modifiedTimeSet;
	}
	public void setModifiedTime() {
		setModifiedTime(new Date(0));
	}
	public void setModifiedTime(Date date) {
		if (modifiedTimeSet) throw new IllegalStateException("time set already");
		modifiedTimeSet = true;
		modifiedTime = date;
	}
	public Date getModifiedTime() {
		return modifiedTime;
	}
	
	public boolean isSizeSet() {
		return sizeSet;
	}
	public void setSize() {
		setSize(-1);
	}
	public void setSize(long size) {
		if (sizeSet) throw new IllegalStateException("size set already");
		this.sizeSet = true;
		this.size = size;
	}
	public long getSize() {
		return size;
	}
	
	@Override
	public String toString() {
		return getPath();
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
	
	public static enum FTPObjectType {
		UNKNOWN, FILE, DIRECTORY;
	}
}
