package com.hsun324.ftp.ftplite;

import java.util.ArrayList;
import java.util.List;

import com.hsun324.ftp.ftplite.FTPFile;

/**
 * This abstract class encapsulates transformations from a generic
 * byte[] data format into more useful data structures through the
 * use of a <code>transform(FTPState, byte[])</code> function.
 * @author hsun324
 * @version 0.7
 * @param <T> the resulting data structure type
 */
public abstract class FTPTransformation<T> {
	/**
	 * A generic transformation for file data that converts byte arrays
	 * into {@link FTPFile}s.
	 */
	public static final FTPTransformation<FTPFile> FILE_TRANSFORMATION = new FTPTransformation<FTPFile>() {
		@Override
		public FTPFile transform(FTPState state, byte[] data) throws Exception {
			if (state.typeImage) return new FTPFile(data);
			return new FTPFile(data, FTPCharset.ASCII);
		}
	};

	/**
	 * A generic transformation for control ASCII responses that converts byte arrays
	 * into <code>String</code>s.
	 */
	public static final FTPTransformation<String> ASCII_TRANSFORMATION = new FTPTransformation<String>() {
		@Override
		public String transform(FTPState state, byte[] data) throws Exception {
			return new String(data, FTPCharset.ASCII);
		}
	};

	/**
	 * A {@link FTPEntity} prototype array.
	 */
	private static final FTPEntity[] FTPENTITY_PROTOTYPE_ARRAY = new FTPEntity[0];
	/**
	 * A generic transformation for file list responses that converts byte arrays
	 * into {@link FTPEntity}<code>[]</code>s.
	 */
	public static final FTPTransformation<FTPEntity[]> FILELIST_TRANSFORMATION = new FTPTransformation<FTPEntity[]> () {
		@Override
		public FTPEntity[] transform(FTPState state, byte[] data) throws Exception {
			// TODO: proper newline splitting
			String[] lines = new String(data).split("\r\n?|\n\r?");
			List<FTPEntity> list = new ArrayList<FTPEntity>();
			int length = lines.length;
			for (int i = 0; i < length; i++) {
				FTPEntity entity = FTPEntity.parseEntity(state.workingDirectory, lines[i]);
				if (entity != null) list.add(entity);
			}
			return list.toArray(FTPENTITY_PROTOTYPE_ARRAY);
		}
	};

	/**
	 * A generic transformation function that takes a state and byte array and turns
	 * the byte array into a more useful data structure.
	 * <p>
	 * Subclasses of <code>FTPTransformation</code> should implement this method.
	 * @param state the current client state
	 * @param data the data array
	 * @return the data structure
	 * @throws Exception
	 */
	public abstract T transform(FTPState state, byte[] data) throws Exception;
}
