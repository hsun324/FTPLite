package com.hsun324.ftplite;

import java.util.Arrays;
import java.util.List;

/**
 * A convenience class that decides the transmission types of different types of files.
 * @author hsun324
 * @version 0.6a
 * @since 0.5
 */
public class FTPTypeDecider {
	/**
	 * Type character for ASCII transmission.
	 */
	public static final char ASCII = 'A';
	/**
	 * Type character for binary transmission.
	 */
	public static final char BINARY = 'I';
	/**
	 * A list of all default extensions to be transmitted using ASCII.
	 */
	private static final List<String> asciiExtensionsList = Arrays.asList(new String[] {
		"ajx", "am", "asa", "asc", "asp", "aspx", "awk", "bat", "c", "cdf", "cf", "cfg",
		"cfm", "cgi", "cnf", "conf", "cpp", "css", "csv", "ctl", "dat", "dhtml", "diz",
		"file", "forward", "grp", "h", "hpp", "hqx", "hta", "htaccess", "htc", "htm",
		"html", "htpasswd", "htt", "htx", "in", "inc", "info", "ini", "ink", "java",
		"js", "jsp", "log", "logfile", "m3u", "m4", "m4a", "mak", "map", "model", "msg",
		"nfo", "nsi", "info", "old", "pas", "patch", "perl", "php", "php2", "php3", "php4",
		"php5", "php6", "phtml", "pix", "pl", "pm", "po", "pwd", "py", "qmail", "rb",
		"rbl", "rbw", "readme", "reg", "rss", "rtf", "ruby", "session", "setup", "sh",
		"shtm", "shtml", "sql", "ssh", "stm", "style", "svg", "tcl", "text", "threads",
		"tmpl", "tpl", "txt", "ubb", "vbs", "xhtml", "xml", "xrc", "xsl"});

	private FTPTypeDecider() { };
	
	/**
	 * Adds an extension to the list of ASCII types.
	 * @param extension the extension to add
	 */
	public static void addExtension(String extension) {
		if (!asciiExtensionsList.contains(extension)) asciiExtensionsList.add(extension);
	}
	/**
	 * Removes an extension from the list of ASCII types.
	 * @param extension the extension to remove
	 */
	public static void removeExtension(String extension) {
		asciiExtensionsList.remove(extension);
	}
	/**
	 * Decides what mode files should be transmitted in from the provided
	 * extension and whether the file has a name.
	 * @param extension file extension
	 * @param hasName whether the file has a name
	 * @return the type character
	 */
	public static char decideFTPType(String extension, boolean hasName) {
		if (!hasName || asciiExtensionsList.contains(extension)) return ASCII;
		else return BINARY;
	}
	
}
