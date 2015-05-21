package com.miniIM.register;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

import com.miniIM.client.Client;
import com.miniIM.transfer.Packet;

class RegisterClient {

	final static int PORT = Client.PORT;
	
	Socket clientSocket;
	ObjectOutputStream out;
	ObjectInputStream in;
	
	public final String SERVER_ADDRESS;
	public final String USERNAME;
	public final long DEVICE_ID;
	
	private boolean isUsernameExisted = false;
	
	public RegisterClient(final String SERVER_ADDRESS, final String USERNAME, final String PASSWORD) {
		
		this.SERVER_ADDRESS = SERVER_ADDRESS;
		this.USERNAME = USERNAME;
		
		// create device ID
		Random random = new Random(System.currentTimeMillis());
		long tmp;
		do {
			tmp = random.nextLong();
		} while (tmp < Integer.MAX_VALUE);
		this.DEVICE_ID = tmp;
		
		try {
			// create client socket
			clientSocket = new Socket(SERVER_ADDRESS, PORT);
			System.out.println(USERNAME + " connect to the server(" + SERVER_ADDRESS + ") successfully.");
			
			/* Use Object(Packet) to exchange information */
			// start output stream
			OutputStream os = clientSocket.getOutputStream();
//			BufferedOutputStream bos = new BufferedOutputStream(os);
			this.out = new ObjectOutputStream(os);
			// start input stream
			InputStream is = clientSocket.getInputStream();
//			BufferedInputStream bis = new BufferedInputStream(is);
			this.in = new ObjectInputStream(is);
			
			
			/* register */
			// create register Packet
			Packet registerPacket = new Packet(Packet.REGISTER, DEVICE_ID);
			registerPacket.register(USERNAME, PASSWORD);
			// send register Packet
			out.writeObject(registerPacket);
			out.flush();
			try {
				Packet backPacket = (Packet) in.readObject();
				if (backPacket.TYPE==Packet.REGISTER_BACK) {
					
					System.out.println("Received Packet from the server(" + SERVER_ADDRESS + ") successfully.");
					
					// pick out the information
					this.isUsernameExisted = backPacket.isUsernameExisted();
					
				} else {
					System.err.println("Wrong received information.");
				}
			} catch (ClassNotFoundException e) {
				System.err.println("Fail to read the back Packet from the server(" + SERVER_ADDRESS + ").");
				e.printStackTrace();
			}
			
			
		} catch (UnknownHostException e) {
			System.err.println("Failed to connect to the server(" + SERVER_ADDRESS + ").");
		} catch (IOException e) {
			System.err.println("Failed to create InputStream or OutputStream with the server(" + SERVER_ADDRESS + ")./nDisconnect to the server.");
		}
	}
	
	public boolean isUsernameExisted() {
		return this.isUsernameExisted;
	}
	
}
