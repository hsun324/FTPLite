package com.hsun324.ftp.ftplite;

import java.util.Date;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class that represents files and folders in a directory listing.
 * @author hsun324
 * @version 0.5
 * @since 0.5
 */
public class FTPEntity {
	/**
	 * Pattern for basic FTP list.
	 */
	private static final Pattern LIST_ENTRY_PATTERN = Pattern.compile("([-drwx]{10})\\s+(?:[^\\s]+\\s+){6,7}(.+)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	/**
	 * Pattern for meta FTP list.
	 */
	private static final Pattern META_FILE_LIST_ENTRY_PATTERN = Pattern.compile("(?:((?:[^;]+=[^;]+;)*) )?([^;]+)$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	
	/**
	 * Parses a single line entity descriptor using the entry patterns.
	 * @param currentDirectory the current client directory
	 * @param line the directory line
	 * @return an entity representing the line
	 */
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

	/**
	 * A pattern describing meta FTP list command date formats.
	 */
	private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{4})(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	/**
	 * Gets a date from a meta FTP list data string using the date pattern.
	 */
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
	/**
	 * A convenience function to parse an <code>Integer</code> from a <code>String</code>.
	 * @param s the string
	 * @return a parsed integer
	 */
	private static int i(String s) {
		return Integer.parseInt(s);
	}

	/**
	 * The filename of this entity.
	 */
	private final FTPFilename name;
	/**
	 * The type of this entity.
	 */
	private final FTPEntityType type;
	/**
	 * The time this entity was last modified.
	 */
	private final Date modified;
	/**
	 * The size of this entity.
	 */
	private final int size;
	
	/**
	 * Creates a <code>FTPEntity</code> with the provided name and type.
	 * @param name the entity name
	 * @param type the entity type
	 */
	protected FTPEntity(FTPFilename name, FTPEntityType type) {
		this(name, type, null, 0);
	}
	
	/**
	 * Creates a <code>FTPEntity</code> with the provided name, type, modification time, and size.
	 * @param name the entity name
	 * @param type the entity type
	 * @param modified the entity modified time
	 * @param size the entity size
	 */
	protected FTPEntity(FTPFilename name, FTPEntityType type, Date modified, int size) {
		this.name = name;
		this.type = type;
		this.modified = modified;
		this.size = size;
	}
	
	/**
	 * Gets the entity name.
	 * @return the entity name
	 */
	public FTPFilename getName() {
		return name;
	}
	/**
	 * Gets the entity type.
	 * @return the entity type
	 */
	public FTPEntityType getType() {
		return type;
	}
	/**
	 * Gets the entity modified time.
	 * @return the entity modified time
	 */
	public Date getModified() {
		return modified;
	}
	/**
	 * Gets the entity size.
	 * @return the entity size
	 */
	public int getSize() {
		return size;
	}
	
	@Override
	public String toString() {
		return this.getType().name() + " " + this.getName();
	}
	
	/**
	 * An enumeration of the types of entities supported by <code>FTPEntity</code>.
	 * @author hsun324
	 * @version 0.5
	 * @since 0.5
	 */
	public enum FTPEntityType {
		/**
		 * A file entity type.
		 */
		FILE,
		/**
		 * A directory entity type.
		 */
		DIRECTORY
	}
}
