package com.miniIM.coder;

import java.security.MessageDigest;

public class SHA {

	private static final String KEY_SHA = "com.miniIM";

	public SHA() {
		// TODO Auto-generated constructor stub
	}
	
	public static byte[] encryptSHA(byte[] data) throws Exception {

		MessageDigest sha = MessageDigest.getInstance(KEY_SHA);
		sha.update(data);

		return sha.digest();

	}
}
