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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.miniIM.transfer.*;

public class Client {
	
	public final static int PORT = 23333;
	
	Socket clientSocket;
	ObjectOutputStream out;
	ObjectInputStream in;
	
	private boolean isLoggedIn = false;
	
	public final String SERVER_ADDRESS;
	public final String USERNAME;
	public final long DEVICE_ID;
	
	private String[][] userList = new String[0][0];
	public List<String> messageList = new ArrayList<String>();
	private List<String> sendList = new ArrayList<String>();
	
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
//			BufferedOutputStream bos = new BufferedOutputStream(os);
			this.out = new ObjectOutputStream(os);
			
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
						
						System.out.println(USERNAME + " log in successfully.");
						
						/* start handle received packet */
						new Thread(new HandlePacket()).start();
						
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
	
	/* handle received packet */
	class HandlePacket extends Thread {
		
		@Override
		public void run() {
			
			/* start sending heart beat */
			new Thread(new Heartbeat()).start();
			
			/* start listening to server's heart beat */
			HeartbeatListener hbl = new HeartbeatListener();
			new Thread(hbl).start();
			
			/* start MessageSender to listen to sendList*/
			new Thread(new MessageSender()).start();
			
			try {
				
				while (true) {
					
					/* read received Packet */
					Packet receivedPacket = (Packet) in.readObject();
					
					switch (receivedPacket.TYPE) {
					case Packet.HEARTBEAT_BACK:
						hbl.setLastPacketTime(System.currentTimeMillis());
						userList = receivedPacket.getUserList();
						break;

					case Packet.CHATROOM_MESSAGE_BACK:
						messageList.add(receivedPacket.getUsername() + DEVICE_ID + receivedPacket.getMessage());
						break;
						
					default:
						break;
					}
					
				}
				
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}

	/* Message Sender*/
	class MessageSender extends Thread {
		@Override
		public void run() {
			while (true) {
				while (!sendList.isEmpty()) {
					Packet outPacket = new Packet(Packet.CHATROOM_MESSAGE, DEVICE_ID);
					outPacket.addMessage(sendList.get(0), USERNAME);
					try {
						out.writeObject(outPacket);
						out.flush();
						sendList.remove(0);
					} catch (IOException e) {
						System.err.println("[Client "+USERNAME+"] Fail to send message.");
						if (clientSocket.isClosed()) {
							e.printStackTrace();
						}
					}
				}
				
				try {
					MessageSender.sleep(500);
				} catch (InterruptedException e) {
					System.err.println("[MessageSender "+USERNAME+"] Fail to sleep.");
					e.printStackTrace();
				}
			}
		}
	}
	
	/* Send Heartbeat to server */
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
	
	/* listen to heartbeat from server */
	class HeartbeatListener extends Thread {
		private long lastPacketTime;
		final int INTERVAL_TIME = 1*1000;
		final long TIMEOUT = 40*1000L;
		
		public HeartbeatListener() {
			
		}
		
		@Override
		public void run() {
			try {
				
				lastPacketTime = System.currentTimeMillis();
				
				do {
					if (System.currentTimeMillis()-lastPacketTime>TIMEOUT) {
						in.close();
						out.close();
						clientSocket.close();
						System.out.println("Lost one connection.");
						
						/* TODO reconncet */
//						System.out.println("Try to Reconnect");
						
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
	
	
	/* get user list */
	public String[][] getUserList() {
		return this.userList;
	}
	
	/* add message into sendList to send */
	public void sendMessage(String str) {
		this.sendList.add(str);
	}
	
	/*  */
	public boolean isLoggedIn() {
		return this.isLoggedIn;
	}
	
}
