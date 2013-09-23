package com.hsun324.ftp.ftplite.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hsun324.ftp.ftplite.FTPResponse;
import com.hsun324.ftp.ftplite.FTPResult;
import com.hsun324.ftp.ftplite.client.FTPInterface;
import com.hsun324.ftp.ftplite.client.FTPInterface.Feature;

/**
 * This {@link Command} handles the passive
 * mode PASV/EPSV command.
 * @author hsun324
 * @version 0.7
 */
public class ModeCommandPassive extends ModeCommand {
	
	private static final Pattern PASSIVE_REPONSE_PATTERN = Pattern.compile("[^(]*\\(((?:[0-9]{1,3},?){4}),([0-9]{1,3}),([0-9]{1,3})\\)");
	private static final Pattern EXTENDED_PASSIVE_REPONSE_PATTERN = Pattern.compile("[^(]*\\((.)\\1{2}([0-9]{1,5})\\1\\)");

	@Override
	public String getCommandContent(FTPInterface inter) {
		if (inter.isFeatureSupported(Feature.EXTENDED_PASSIVE)) return "EPSV";
		return "PASV";
	}
	@Override
	public FTPResult handleResponse(FTPInterface inter, FTPResponse response) {
		try {
			if (inter.isFeatureSupported(Feature.EXTENDED_PASSIVE)) {
				if (response.getCode() == 229) {
					Matcher matcher = EXTENDED_PASSIVE_REPONSE_PATTERN.matcher(response.getContent());
					if (matcher.find()) {
						inter.setDataHost(inter.getHost());
						inter.setDataPort(Integer.parseInt(matcher.group(2)));
						return FTPResult.SUCCEEDED;
					}
				}
			} else {
				if (response.getCode() == 227) {
					Matcher matcher = PASSIVE_REPONSE_PATTERN.matcher(response.getContent());
					if (matcher.find()) {
						inter.setDataHost(matcher.group(1).replace(',', '.'));
						inter.setDataPort(Integer.parseInt(matcher.group(2)) * 256 + Integer.parseInt(matcher.group(3)));
						return FTPResult.SUCCEEDED;
					}
				}
			}
		} catch (NumberFormatException e) { }
		return FTPResult.FAILED;
	}
	
	@Override
	public boolean isActive() {
		return false;
	}
}
