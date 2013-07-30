package com.hsun324.ftplite;

public abstract class FTPTransformation<T> {
	public abstract T transform(byte[] data) throws Exception;
}
