package com.miniIM.client;

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

import com.miniIM.transfer.Packet;

public class Client {
	
	public final static int PORT = 23333;
	
//	BufferedReader br;
//	PrintStream ps;
	Socket clientSocket;
	ObjectOutputStream out;
	ObjectInputStream in;
	
	private boolean isLoggedIn = false;
	
	public final String SERVER_ADDRESS;
	public final String USERNAME;
	public final long DEVICE_ID;
	
	public Client(final String SERVER_ADDRESS, final String USERNAME, final String PASSWORD) {
		
		this.SERVER_ADDRESS = SERVER_ADDRESS;
		this.USERNAME = USERNAME;
		
		/* create device ID */
		Random random = new Random(System.currentTimeMillis());
		long tmp;
		do {
			tmp = random.nextLong();
		} while (tmp < Integer.MAX_VALUE);
		this.DEVICE_ID = tmp;
		
		try {
			
			/* create client socket */
			clientSocket = new Socket(SERVER_ADDRESS, PORT);
			System.out.println(USERNAME + " connect to the server(" + SERVER_ADDRESS + ") successfully.");
			
			/* Use Object(Packet) to exchange information */
			
			/* start output stream */
			OutputStream os = clientSocket.getOutputStream();
			BufferedOutputStream bos = new BufferedOutputStream(os);
			this.out = new ObjectOutputStream(bos);
			
			/* start input stream */
			InputStream is = clientSocket.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			this.in = new ObjectInputStream(bis);
			
			
			/* ====== log in ====== */
			
			/* send login information */
			Packet packet = new Packet(Packet.LOGIN, DEVICE_ID);
			packet.login(USERNAME, PASSWORD);
			out.writeObject(packet);
			out.flush();
			
			try {
				
				/* receive back Packet */
				Packet backPacket = (Packet) in.readObject();
				
				if (backPacket.TYPE==Packet.LOGIN_BACK) {
					
					this.isLoggedIn = backPacket.isLoginSuccessful();
					
					if (isLoggedIn) {
						
						/* start sending heart beat */
						new Thread(new Heartbeat()).start();
						
						/* start listening to server's heart beat */
						HeartbeatListener hbl = new HeartbeatListener(clientSocket, in, out);
						new Thread(hbl).start();
						
						System.out.println(USERNAME + " log in successfully.");
						
						// TODO
						
					} else {
						System.err.println("Fail to log in.");
					}
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
			System.err.println("Failed to connect to the server(" + SERVER_ADDRESS + ").");
		}
	}
	
	public boolean isLoggedIn() {
		return this.isLoggedIn;
	}
	
	class Heartbeat extends Thread {
		final int INTERVAL_TIME = 15*1000;
		
		public void run() {
			try {
				while (true) {
					
					/* send heart beat */
					out.writeObject(new Packet(Packet.HEARTBEAT, DEVICE_ID));
					out.flush();
					
					Thread.sleep(INTERVAL_TIME);
				}
			} catch (IOException e) {
				System.err.println("Fail to send heartbeat.");
				e.printStackTrace();
			} catch (InterruptedException e) {
				System.err.println("Thread sleep failed.");
				e.printStackTrace();
			}
		
		};
		
	}
	
	class HeartbeatListener extends Thread {
		Socket client;
		ObjectInputStream in;
		ObjectOutputStream out;
		private long lastPacketTime;
		final int INTERVAL_TIME = 1*1000;
		final long TIMEOUT = 40*1000L;
		
		public HeartbeatListener(Socket client, ObjectInputStream ois, ObjectOutputStream oos) {
			this.client = client;
			this.in = ois;
			this.out = oos;
		}
		
		@Override
		public void run() {
			try {
				
				lastPacketTime = System.currentTimeMillis();
				
				do {
					if (System.currentTimeMillis()-lastPacketTime>TIMEOUT) {
						in.close();
						out.close();
						client.close();
						System.out.println("Lost one connection.");
						break;
					}
					HeartbeatListener.sleep(INTERVAL_TIME);
				} while (true);
				
			} catch (IOException e) {
				System.err.println("Fail to close the Socket or it had closed already.");
			} catch (InterruptedException e) {
				System.err.println("Fail to sleep the HeartbeatListener Thread.");
				e.printStackTrace();
			}
			
		}
		
		public void setLastPacketTime(long t) {
			this.lastPacketTime = t;
		}
	}
	
}
