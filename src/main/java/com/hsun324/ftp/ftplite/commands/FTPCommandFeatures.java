package com.hsun324.ftp.ftplite.commands;

import com.hsun324.ftp.ftplite.FTPCommand;
import com.hsun324.ftp.ftplite.FTPResponse;
import com.hsun324.ftp.ftplite.FTPResult;
import com.hsun324.ftp.ftplite.FTPState;
/**
 * This {@link FTPCommand} handles the features
 * FEAT command.
 * @author hsun324
 * @version 0.7
 */
public class FTPCommandFeatures extends FTPCommand {
	@Override
	public String getCommandContent(FTPState state) {
		return "FEAT";
	}
	@Override
	public boolean isValidContext(FTPState state) {
		return state.authCompleted;
	}
	
	@Override
	public FTPResult handleResponse(FTPState state, FTPResponse response) {
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
					
					if (tag.equals("EPSV")) state.featureExtPassive = true;
					if (tag.equals("MDTM")) state.featureModificationTime = true;
					if (tag.equals("MLST")) {
						state.featureFileMetadata = true;
						state.featureFileMetadataParams = content.split(";");
					}
					if (tag.equals("SIZE")) state.featureFileSize = true;
					if (tag.equals("UTF8")) state.featureUTF8 = true;
				}
			}
			
			return FTPResult.SUCCEEDED;
		}
		return FTPResult.FAILED;
	}
}
