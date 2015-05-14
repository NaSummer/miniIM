package com.miniIM.client;

//import java.io.BufferedReader;
import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
//import java.io.OutputStream;
//import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

import com.miniIM.transfer.Packet;

public class Client {
	
	final static int PORT = 23333;
	
//	BufferedReader br;
//	PrintStream ps;
	Socket clientSocket;
	ObjectOutputStream out;
	ObjectInputStream in;
	
	public final String SERVER_ADDRESS;
	public final String USERNAME;
	public final long DEVICE_ID;
	
	public Client(final String SERVER_ADDRESS, final String USERNAME, final String PASSWORD) {
		
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
			this.out = new ObjectOutputStream(clientSocket.getOutputStream());
			// start input stream
			this.in = new ObjectInputStream(clientSocket.getInputStream());
			
			
			/* log in */
			// send login information
			Packet packet = new Packet(Packet.LOGIN, DEVICE_ID);
			packet.login(USERNAME, PASSWORD);
			out.writeObject(packet);
			try {
				Packet backPacket = (Packet) in.readObject();
				if (backPacket.TYPE==Packet.LOGIN_BACK) {
					
					// start sending heart beat
					new Thread(new Heartbeat()).start();
					
					System.out.println(USERNAME + " log in successfully.");
					
				} else {
					System.err.println("Fail to log in.");
				}
			} catch (ClassNotFoundException e) {
				System.err.println("Fail to log in.");
				e.printStackTrace();
			}
			
			
			
//			// start input stream
//			InputStream is = clientSocket.getInputStream();
//			InputStreamReader isr = new InputStreamReader(is);
//			br = new BufferedReader(isr);
//			
//			// start output stream
//			OutputStream os = clientSocket.getOutputStream();
//			ps = new PrintStream(os);
			
		} catch (UnknownHostException e) {
			System.err.println("Failed to connect to the server(" + SERVER_ADDRESS + ").");
		} catch (IOException e) {
			System.err.println("Failed to create InputStream or OutputStream with the server(" + SERVER_ADDRESS + ")./nDisconnect to the server.");
		}
	}
	
	class Heartbeat extends Thread {
		public void run() {
			try {
				while (true) {
					// send heart beat
					out.writeObject(new Packet(Packet.HEARTBEAT, DEVICE_ID));
					// receive response from the server
					Packet receivedBeat = (Packet) in.readObject();
					// if received response from server, waiting 15sec to send again
					if (receivedBeat.IS_READ_BY_SERVER) {
						Thread.sleep(15000);
					}
				}
			} catch (IOException e) {
				System.err.println("Fail to send heartbeat.");
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				System.err.println("Fail to read the response heartbeat.");
				e.printStackTrace();
			} catch (InterruptedException e) {
				System.err.println("Thread sleep failed.");
				e.printStackTrace();
			}
		
		};
		
	}
	
}
