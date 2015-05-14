package com.miniIM.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
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
	
	// keep client connection
	List<Socket> clientConnection = new ArrayList<Socket>();
	// store client log in Packet (username & deviceID)
	List<Packet> clientDeviceID = new ArrayList<Packet>();
	
	public Server() {
		
		// create server deviceID
		Random random = new Random(System.currentTimeMillis());
		long tmp;
		do {
			tmp = random.nextLong();
		} while (tmp < Integer.MAX_VALUE);
		this.DEVICE_ID = tmp;
		
		// create ServerSocket
		try {
			server = new ServerSocket(PORT);
			System.out.println("Succeeded in starting the server.");
			
			// get local host address
			InetAddress addr = InetAddress.getLocalHost();
			System.out.println("Server IP address: " + addr.getHostAddress());
			
			// start to listen to client Socket using SocketListener inner class
			new Thread(new SocketListener()).start();
			
		} catch (IOException e) {
			System.err.println("Failed in starting the server.");
			e.printStackTrace();
		}
	}
	
	class SocketListener implements Runnable {
		@Override
		public void run() {
			Socket client;
			
			try {
				while (true) {
					// listen to the connection of client
					client = server.accept();
					// if the client connects the server, add it in to list
					clientConnection.add(client);
					// give the client socket to Sort to judge whether the socket is login or register 
					new Sort(client);
				}
			} catch (IOException e) {
				e.printStackTrace();
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
				// start ObjectInputStream
				ObjectInputStream in = new ObjectInputStream(client.getInputStream());
				// start ObjectOutputStream
				ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
				
				// read the first Packet
				Packet firstPacket = (Packet) in.readObject();
				
				
			} catch (IOException e) {
				System.err.println("Fail to create ObjectInputStream or ObjectOutputStream");
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				System.err.println("Fail to read first Packet.");
				e.printStackTrace();
			}
		}
	}
	
	
	
	
	
	class HandleUserPacket implements Runnable {
		Socket client;
		ObjectInputStream in;
		ObjectOutputStream out;
		
		public HandleUserPacket(Socket client, ObjectInputStream ois, ObjectOutputStream oos) {
			this.client = client;
			this.in = ois;
			this.out = oos;
		}
		
		/* old cold */
		@Override
		public void run() {
			try {
				
				// Start Input Stream
				InputStream is = client.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				System.out.println("The no." + clientConnection.size() + " client connected to the server.");
				
				// read the message
				String message;
				do {
					message = br.readLine();
					System.out.println("Server receive a message from a client: " + message);
					// Send message to all connected clients
					for (int i = 0; i < clientConnection.size(); i++) {
						Socket tmpClient = clientConnection.get(i);
						
						// Start Output Stream
						OutputStream os = tmpClient.getOutputStream();
						PrintStream ps = new PrintStream(os);
						ps.println(message);
					}
				} while (true);
				
			} catch (IOException e) {
				
				System.out.println("One client disconnected with the server.");
			}
		}
	}


	public static void main(String[] args) {
		new Server();
	}

}
