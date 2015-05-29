package com.miniIM.server;

import java.net.Socket;

import com.miniIM.transfer.User;

class ServerUser extends User{
	
	public final Socket SOCKET;
	
	public ServerUser(String username, Socket socket, long deviceID) {
		super(username, deviceID);
		this.SOCKET = socket;
	}
}
