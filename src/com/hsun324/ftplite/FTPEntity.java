package com.hsun324.ftplite;

import java.util.Date;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FTPEntity {
	private static final Pattern LIST_ENTRY_PATTERN = Pattern.compile("([-drwx]{10})\\s+(?:[^\\s]+\\s+){6,7}(.+)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	private static final Pattern META_FILE_LIST_ENTRY_PATTERN = Pattern.compile("(?:((?:[^;]+=[^;]+;)*) )?([^;]+)$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	public static FTPEntity parseEntity(FTPFilename currentDirectory, String line) {
		try {
			Matcher matcher = LIST_ENTRY_PATTERN.matcher(line);
			if (matcher.find()) {
				FTPFilename name = new FTPFilename(currentDirectory, matcher.group(2));
				if (matcher.group(1).toLowerCase().contains("d")) return new FTPEntity(name, FTPEntityType.DIRECTORY);
				else return new FTPEntity(name, FTPEntityType.FILE);
			} else {
				matcher = META_FILE_LIST_ENTRY_PATTERN.matcher(line);
				if (matcher.find()) {
					FTPFilename name = new FTPFilename(currentDirectory, matcher.group(2));
					FTPEntityType type = FTPEntityType.FILE;
					Date modified = null;
					int size = 0;
	
					String[] split = matcher.group(1).split(";");
					for(String fact : split) {
						String[] factPair = fact.split("=");
						if (factPair.length == 2) {
							if (factPair[0].equalsIgnoreCase("type")) {
								if (factPair[1].equalsIgnoreCase("dir")) type = FTPEntityType.DIRECTORY;
								else if (!factPair[1].equalsIgnoreCase("file")) return null;
							} else if (factPair[0].equalsIgnoreCase("size")) {
								size = Integer.parseInt(factPair[1]);
							} else if (factPair[0].equalsIgnoreCase("modified")) {
								modified = getDate(factPair[1]);
							}
						}
					}
					return new FTPEntity(name, type, modified, size);
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


	private final FTPFilename name;
	private final FTPEntityType type;
	private final Date modified;
	private final int size;
	
	protected FTPEntity(FTPFilename name, FTPEntityType type) {
		this(name, type, null, 0);
	}
	protected FTPEntity(FTPFilename name, FTPEntityType type, Date modified, int size) {
		this.name = name;
		this.type = type;
		this.modified = modified;
		this.size = size;
	}
	
	public FTPFilename getName() {
		return name;
	}
	public FTPEntityType getType() {
		return type;
	}
	public Date getModified() {
		return modified;
	}
	public int getSize() {
		return size;
	}
	
	@Override
	public String toString() {
		return this.getType().name() + " " + this.getName();
	}
	
	public enum FTPEntityType {
		FILE,
		DIRECTORY
	}
}
