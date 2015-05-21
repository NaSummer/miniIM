package com.miniIM.transfer;

import java.io.Serializable;

public class Packet implements Serializable{
		
	/**
	 * 
	 */
	private static final long serialVersionUID = -3900021794281181446L;
	
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
	
	/* Variable */
	private String username; // login, register, allChat,
	private String password; // login, register
	private boolean isLoginSuccessful; // loginBack, registerBack
	private boolean isUsernameExisted; // loginBack, registerBack
	private String message; // allChat
	
	


	public Packet(final int TYPE, final long DEVICE_ID) {
		this.TYPE = TYPE;
		this.DEVICE_ID = DEVICE_ID;
		if (TYPE%2==0) {
			this.IS_READ_BY_SERVER = false;
		} else {
			this.IS_READ_BY_SERVER = true;
		}
	}
	
	
	/* login */
	public void login(String username, String password) {
		if (this.TYPE==LOGIN) {
			this.username = username;
			this.password = password;
		}
	}
	
	/* login back */
	public void loginBack(boolean isSuccessful) {
		if (this.TYPE==LOGIN_BACK) {
			this.isLoginSuccessful = isSuccessful;
		}
	}
	
	/* register */
	public void register(String username, String password) {
		if (this.TYPE==REGISTER) {
			this.username = username;
			this.password = password;
		}
	}
	
	/* register back */
	public void registerBack(Boolean isUsernameExisted) {
		if (this.TYPE==REGISTER_BACK) {
			this.isUsernameExisted = isUsernameExisted;
		}
	}
	
	/* room chat */
	public void setRoomChat(String message, String username) {
		this.message = message;
		this.username = username;
	}
	
	public void addMessage(String str) {
		this.message = str;
	}
	
	public String getMessage() {
		return this.message;
	}
	
	public String getUsername() {
		return username;
	}
	
	
	public String getPassword() {
		if ( (this.TYPE==LOGIN) || (this.TYPE==REGISTER) ) {
			return password;
		} else {
			return null;
		}
	}
	
	
	public boolean isLoginSuccessful() {
		return isLoginSuccessful;
	}
	
	
	public boolean isUsernameExisted() {
		return isUsernameExisted;
	}
	
}
