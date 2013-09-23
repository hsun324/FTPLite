package com.hsun324.ftp.ftplite.commands;

import com.hsun324.ftp.ftplite.FTPResponse;
import com.hsun324.ftp.ftplite.FTPResult;
import com.hsun324.ftp.ftplite.client.FTPInterface;
import com.hsun324.ftp.ftplite.client.FTPInterface.ClientState;
import com.hsun324.ftp.ftplite.client.FTPInterface.Feature;
/**
 * This {@link Command} handles the features
 * FEAT command.
 * @author hsun324
 * @version 0.7
 */
public class TextCommandFeatures extends TextCommand {
	@Override
	public String getCommandContent(FTPInterface inter) {
		return "FEAT";
	}
	@Override
	public boolean isValidContext(FTPInterface inter) {
		return inter.getClientState() == ClientState.READY;
	}
	
	@Override
	public FTPResult handleResponse(FTPInterface inter, FTPResponse response) {
		if (response.getCode() == 211) {
			String[] features = response.getContent().split("\n");
			if (features.length > 2) {
				int length = features.length - 1;
				for (int i = 1; i < length; i++) {
					String feature = features[i].trim().toUpperCase();
					int index = feature.indexOf(" ");
					String tag = feature;
					String content = "";
					if (index > -1) {
						tag = feature.substring(0, index);
						content = feature.substring(index + 1);
					}
					
					if (tag.equals("EPSV")) inter.setFeatureSupported(Feature.EXTENDED_PASSIVE, true);
					if (tag.equals("MDTM")) inter.setFeatureSupported(Feature.MODIFICATION_TIME, true);
					if (tag.equals("MLST")) {
						inter.setFeatureSupported(Feature.METADATA_LIST, true);
						inter.setMetadataParameters(content.split(";"));
					}
					if (tag.equals("SIZE")) inter.setFeatureSupported(Feature.FILE_SIZE, true);
					if (tag.equals("UTF8")) inter.setFeatureSupported(Feature.UTF8, true);
				}
			}
			
			return FTPResult.SUCCEEDED;
		}
		return FTPResult.FAILED;
	}
}
