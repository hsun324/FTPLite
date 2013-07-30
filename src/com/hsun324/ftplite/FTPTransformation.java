package com.hsun324.ftplite;

import java.util.ArrayList;
import java.util.List;

public abstract class FTPTransformation<T> {
	public static final FTPTransformation<List<String>> FILE_LIST_TRANFORMATION = new FTPTransformation<List<String>>() {
		@Override
		public List<String> transform(byte[] data) throws Exception {
			// TODO: better newline splitting
			String[] lines = new String(data).trim().split("\n\r");
			List<String> list = new ArrayList<String>();
			int length = lines.length;
			for (int i = 0; i < length; i++)
				list.add(lines[i]);
			
			return list;
		}
	};
	public static final FTPTransformation<String> FILE_TRANSFORMATION = new FTPTransformation<String>() {
		@Override
		public String transform(byte[] data) throws Exception {
			// TODO: better newline culling
			return new String(data).replace("\r\n", "");
		}
	};

	public abstract T transform(byte[] data) throws Exception;
}
