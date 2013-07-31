package com.hsun324.ftplite.commands;

import com.hsun324.ftplite.FTPCommand;
import com.hsun324.ftplite.FTPResponse;
import com.hsun324.ftplite.FTPResult;
import com.hsun324.ftplite.FTPState;

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
					
					switch (tag) {
					case "EPSV":
						state.featureExtPassive = true; break;
					case "MDTM":
						state.featureModificationTime = true; break;
					case "MLST":
						state.featureFileMetadata = true; break;
					case "SIZE":
						state.featureFileSize = true; break;
					case "UTF8":
						state.featureUTF8 = true; break;
					}
				}
			}
			
			return FTPResult.SUCCEEDED;
		}
		return FTPResult.FAILED;
	}
}
