package com.miniIM.transfer;

public class Packet {
		
	public final static int HEARTBEAT = 0;
	public final static int HEARTBEAT_BACK = 1;
	public final static int LOGIN = 2;
	public final static int LOGIN_BACK = 3;
	public final static int LOGOUT = 4;
	public final static int LOGOUT_BACK = 5;
	public final static int REGISTER = 6;
	public final static int REGISTER_BACK = 7;
	public final static int CHATROOM_MESSAGE = 8;
	public final static int CHATROOM_MESSAGE_BACK = 9;
	public final static int P2P_MESSAGE = 10;
	public final static int P2P_MESSAGE_BACK = 11;
	public final static int GROUP_MESSAGE = 12;
	public final static int GROUP_MESSAGE_BACK = 13;
	
	public final int TYPE;
	public final long DEVICE_ID;
	public final boolean IS_READ_BY_SERVER;
	
	public Packet(final int TYPE, final long DEVICE_ID) {
		this.TYPE = TYPE;
		this.DEVICE_ID = DEVICE_ID;
		if (TYPE%2==0) {
			this.IS_READ_BY_SERVER = false;
		} else {
			this.IS_READ_BY_SERVER = true;
		}
	}
	
	// variables for login and register
	private String username;
	private String password;
	public void login(String username, String password) {
		if (this.TYPE==LOGIN) {
			this.username = username;
			this.password = password;
		}
	}
	
	public void register(String username, String password) {
		if (this.TYPE==REGISTER) {
			this.username = username;
			this.password = password;
		}
	}
	
	private String getUsername() {
		return this.username;
	}
	
	// variables for register back, judge whether username existed
	private boolean isUsernameExisted;
	public void registerBack(Boolean isUsernameExisted) {
		if (this.TYPE==REGISTER_BACK) {
			this.isUsernameExisted = isUsernameExisted;
		}
	}
	public boolean isUsernameExisted() {
		return this.isUsernameExisted;
	}
	
	
	// TODO
	public void roomChat() {
		
	}
	
}
