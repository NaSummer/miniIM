package com.miniIM.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import com.miniIM.transfer.Packet;

public class Server {

	final static int PORT = 23333;
	
	public final long DEVICE_ID;
	
	ServerSocket server;
	
	/* keep client connection */
	List<Socket> clientConnection = new ArrayList<Socket>();
	List<ObjectOutputStream> clientOutputStream = new ArrayList<ObjectOutputStream>();
	
	/* user list */
	List<ServerUser> userList = new ArrayList<ServerUser>();
	
	/* Constructor */
	public Server() {
		
		/* create server deviceID */
		Random random = new Random(System.currentTimeMillis());
		long tmp;
		do {
			tmp = random.nextLong();
		} while (tmp < Integer.MAX_VALUE);
		this.DEVICE_ID = tmp;
		
		/* create ServerSocket */
		try {
			server = new ServerSocket(PORT);
			System.out.println("Create ServerSocket");
			
			/* get local host address */
			InetAddress addr = InetAddress.getLocalHost();
			System.out.println("Server IP address: " + addr.getHostAddress());
			
			/* start to listen to client Socket using SocketListener inner class */
			new Thread(new SocketListener()).start();
			
		} catch (IOException e) {
			System.err.println("Failed in starting the server.");
			e.printStackTrace();
		}
	}
	
	/* listen to new client connection */
	class SocketListener implements Runnable {
		@Override
		public void run() {
			Socket client;
			
			try {
				while (true) {
					/* listen to the connection of client */
					client = server.accept();
					
					/* add client in to list */
					clientConnection.add(client);
					System.out.println("The no." + clientConnection.size() + " client connected to the server.");
					
					/* give the client socket to Sort to judge whether the socket is login or register */ 
					new Sort(client);
				}
			} catch (IOException e) {
//				e.printStackTrace();
				System.err.println("Failed in connecting to the client.");
			}
		}
	}
	
	/* sort login and register */
	class Sort {
		Socket client;
		
		public Sort(Socket client) {
			this.client = client;
			
			try {
				
				/* start ObjectOutputStream */
				OutputStream os = client.getOutputStream();
//				BufferedOutputStream bos = new BufferedOutputStream(os);
				ObjectOutputStream out = new ObjectOutputStream(os);
//				ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(client.getOutputStream()));
				clientOutputStream.add(out);
				System.out.println("/* start ObjectOutputStream */");
				
				/* start ObjectInputStream */
				InputStream is = client.getInputStream();
				BufferedInputStream bis = new BufferedInputStream(is);
				ObjectInputStream in = new ObjectInputStream(bis);
//				ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(client.getInputStream()));
				System.out.println("/* start ObjectInputStream */");
				
				/* read the first Packet */
				Packet firstPacket = (Packet) in.readObject();
				System.out.println("Packet firstPacket = (Packet) in.readObject();");
				
				/* judge */
				if (firstPacket.TYPE==Packet.REGISTER) {
					System.out.println("firstPacket.TYPE==Packet.REGISTER");
					
					System.out.println("/* create back Packet */");
					/* create back Packet */
					System.out.println("Packet backPacket = new Packet(Packet.REGISTER_BACK, DEVICE_ID);");
					Packet backPacket = new Packet(Packet.REGISTER_BACK, DEVICE_ID);
					System.out.println("backPacket.registerBack(isUserExisted(firstPacket.getUsername()));");
					/* judge is User Existed already */
					if (isUserExisted(firstPacket.getUsername())) {
						backPacket.registerBack(true);
					} else {
						writeUserInfo(firstPacket.getUsername(), firstPacket.getPassword());// write user info into file
						backPacket.registerBack(false);
					}
					
					/* send back Packet */
					System.out.println("/* send back Packet */");
					out.writeObject(backPacket);
					System.out.println("out.writeObject(backPacket);");
					out.flush();
					System.out.println("out.flush();");
					
					/* close Socket */
					System.out.println("/* close Socket */");
					clientConnection.remove(client);
					clientOutputStream.remove(out);
					System.out.println("clientConnection.remove(client);");
					in.close();
					System.out.println("in.close();");
					out.close();
					System.out.println("out.close();");
					client.close();
					System.out.println("client.close();");
					
				} else if (firstPacket.TYPE==Packet.LOGIN) {
					Packet backPacket = new Packet(Packet.LOGIN_BACK, DEVICE_ID);
					/* authenticate */
					if (isInfoRight(firstPacket.getUsername(), firstPacket.getPassword())) {
						
						/* create ServerUser */
						ServerUser user = new ServerUser(firstPacket.getUsername(), client, firstPacket.DEVICE_ID);
						
						/* add user to userList */
						userList.add(user);
						
						/* create success info back Packet */
						backPacket.loginBack(true, generateUserList());
						
						/* send back Packet */
						out.writeObject(backPacket);
						out.flush();
						
						/* pass client to HandleUserPacket to keep connection */
						new Thread(new HandleUserPacket(user, client, in, out)).start();
						
					} else { // when authenticate fail 
						
						/* create fail info back Packet */
						backPacket.loginBack(false);
						
						/* send back Packet */
						out.writeObject(backPacket);
						out.flush();
					}
					
				} else {
					System.err.println("Wrong received first Packet type.");
				}
				
			} catch (IOException e) {
				clientConnection.remove(client);
				System.err.println("Disconnect with one client.");
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				clientConnection.remove(client);
				System.err.println("Fail to read first Packet.");
				e.printStackTrace();
			}
		}
	}
	
	/* keep connection and process Packet */
	class HandleUserPacket extends Thread {
		ServerUser user;
		Socket client;
		ObjectInputStream in;
		ObjectOutputStream out;
		
		public HandleUserPacket(ServerUser user, Socket client, ObjectInputStream ois, ObjectOutputStream oos) {
			this.user = user;
			this.client = client;
			this.in = ois;
			this.out = oos;
		}
		
		@Override
		public void run() {
			try {
				
				/* add Heartbeat Listener */
				HeartbeatListener hbl = new HeartbeatListener(user, client, in, out);
				new Thread(hbl).start();
				
				/* listen to client */
				do {
					/* receive Packet */
					Packet receiver = (Packet) in.readObject();
					
//					/* verify Packet */
//					if (receiver.DEVICE_ID) {
//						// TODO
//					}
					
					/* create null back Packet*/
					Packet backPacket = null;
					
					/* check heartbeat */
					if (receiver.TYPE==Packet.HEARTBEAT) {
						hbl.setLastPacketTime(System.currentTimeMillis());
						backPacket = new Packet(Packet.HEARTBEAT_BACK, DEVICE_ID);
						backPacket.heartbeatBack(generateUserList());
						
						/* send back Heartbeat */
						out.writeObject(backPacket);
						out.flush();
					}
					
					/* check Chat Room Message */
					if (receiver.TYPE==Packet.CHATROOM_MESSAGE) {
						
						new Thread(new SendBackMessage(receiver)).start();
						
					}
					
					
					
					// TODO add new function here
					
				} while (true);
				
			} catch (IOException e) {
				clientConnection.remove(client);
				clientOutputStream.remove(out);
				userList.remove(user);
			} catch (ClassNotFoundException e) {
				System.err.println("Can't read Object from client.");
				clientConnection.remove(client);
				clientOutputStream.remove(out);
				userList.remove(user);
				e.printStackTrace();
			} finally {
				try {
					clientConnection.remove(client);
					clientOutputStream.remove(out);
					userList.remove(user);
					in.close();
					out.close();
					client.close();
				} catch (IOException e) {
					System.err.println("Fail to close the stream or the stream had been closed.");
//					e.printStackTrace();
				}
				System.out.println("One client disconnected with the server.");
				System.out.println("There are " + clientConnection.size() + " connection now after one disconnection.");
			}
		}
	}
	
	/* Send Back Message */
	class SendBackMessage extends Thread {
		
		Packet receiver;
		
		public SendBackMessage(Packet packet) {
			this.receiver = packet;
		}
		
		@Override
		public void run() {
			/* create back Packet */
			Packet backPacket = new Packet(Packet.CHATROOM_MESSAGE_BACK, DEVICE_ID);
			backPacket.setRoomChat(receiver.getMessage(), receiver.getUsername());
			
			/* send message to all client */
			for (int i = 0; i < clientOutputStream.size(); i++) {
				try {
					ObjectOutputStream tmpOut = clientOutputStream.get(i);
					tmpOut.writeObject(backPacket);
					tmpOut.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	
	/* Heartbeat Listener */
	class HeartbeatListener extends Thread {
		ServerUser user;
		Socket client;
		ObjectInputStream in;
		ObjectOutputStream out;
		private long lastPacketTime;
		final int INTERVAL_TIME = 1*1000;
		final long TIMEOUT = 40*1000L;
		
		public HeartbeatListener(ServerUser user, Socket client, ObjectInputStream ois, ObjectOutputStream oos) {
			this.user = user;
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
						clientConnection.remove(client);
						clientOutputStream.remove(out);
						userList.remove(user);
						in.close();
						out.close();
						client.close();
						System.out.println("Lost one connection.");
						System.out.println("There are " + clientConnection.size() + " connection now after one disconnection.");
						break;
					}
					HeartbeatListener.sleep(INTERVAL_TIME);
				} while (true);
				
			} catch (IOException e) {
				System.err.println("Fail to close the Socket or it had closed already.");
			} catch (InterruptedException e) {
				System.err.println("Fail to sleep the HeartbeatListener Thread.");
//				e.printStackTrace();
			}
			
		}
		
		public void setLastPacketTime(long t) {
			this.lastPacketTime = t;
		}
	}
	
	/* generate user list */
	private String[][] generateUserList() {
		String[][] userList = new String[this.userList.size()][2];
		for (int i = 0; i < this.userList.size(); i++) {
			userList[i][0] = this.userList.get(i).USERNAME;
			userList[i][1] = Long.toString(this.userList.get(i).DEVICE_ID);
		}
		return userList;
	}
	
	/*  */
	private boolean isInfoRight(String username, String password) {
		try {
			BufferedReader br = preReadUserInfo();
			String[] tmpInfo = null;
			
			/* read whole BufferedReader */
			String tmpStr = br.readLine();
			while (tmpStr!=null) {
				/* split */
				tmpInfo = tmpStr.split("=");
				if (tmpInfo[0].equals(username)) {
					if (tmpInfo[1].equals(password)) {
						return true;
					} else {
						return false;
					}
				}
				tmpStr = br.readLine();
			}
			return false;
		} catch (IOException e) {
			System.err.println("Fail to read userinfo from file.");
			e.printStackTrace();
			return true;
		}
	}
	
	private boolean isUserExisted(String username) {
		try {
			BufferedReader br = preReadUserInfo();
			
			/* read whole BufferedReader */
			String tmpStr = br.readLine();
			while (tmpStr!=null) {
				// check name
				tmpStr = tmpStr.substring(0, tmpStr.indexOf('='));
				if (tmpStr.equals(username)) {
					return true;
				}
				tmpStr = br.readLine();
			}
			return false;
		} catch (IOException e) {
			System.err.println("Fail to read userinfo from file.");
			e.printStackTrace();
			return true;
		}
	}
	
	private void writeUserInfo(String username, String password) {
		File file = new File("userinfo\\userinfo.userinfo");
		
		/* if file does not exist, create the new file */
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				System.err.println("Fail to create new file (userinfo)");
				e.printStackTrace();
			}
		}
		
		/* Use FileWriter and BufferWriter to write file */
		try {
			FileWriter fileWriter = new FileWriter(file, true);// use "true" to write the new after the original
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write(username + "=" + password + "\n");
			bufferedWriter.flush();
			bufferedWriter.close();
//			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e) {
			System.err.println("Fail to write file");
			e.printStackTrace();
		}
		
	}
	
	private BufferedReader preReadUserInfo() {
		File file = new File("userinfo\\userinfo.userinfo");
		
		/* if file does not exist, create the new file */
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				System.err.println("Fail to create new file (userinfo)");
				e.printStackTrace();
				return null;
			} 
		}
		
		/* read File */
		try {
			return new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			System.err.println("Fail to read Userinfo");
			e.printStackTrace();
			return null;
		}
	}
	
	
	
	public static void main(String[] args) {
		new Server();
	}

}
