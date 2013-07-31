package com.hsun324.ftplite;

import java.util.ArrayList;
import java.util.List;

public abstract class FTPTransformation<T> {
	public static final FTPTransformation<String> FILE_TRANSFORMATION = new FTPTransformation<String>() {
		@Override
		public String transform(byte[] data) throws Exception {
			// TODO: better newline culling
			return new String(data).replace("\r\n", "");
		}
	};
	
	private static final FTPEntity[] FTPENTITY_PROTOTYPE_ARRAY = new FTPEntity[0];
	
	public static class FileListTransformation extends FTPTransformation<FTPEntity[]> {
		protected final FTPFilename currentDirectory;
		
		public FileListTransformation(FTPFilename currentDirectory) {
			this.currentDirectory = currentDirectory;
		}
		
		@Override
		public FTPEntity[] transform(byte[] data) throws Exception {
			// TODO: better newline splitting
			String[] lines = new String(data).split("\n\r|\n|\r");
			
			List<FTPEntity> list = new ArrayList<FTPEntity>();
			
			int length = lines.length;
			for (int i = 0; i < length; i++) {
				FTPEntity entity = FTPEntity.parseEntity(currentDirectory, lines[i]);
				if (entity != null) list.add(entity);
			}
			
			return list.toArray(FTPENTITY_PROTOTYPE_ARRAY);
		}
	};

	public abstract T transform(byte[] data) throws Exception;
}
