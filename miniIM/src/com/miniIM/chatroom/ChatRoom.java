package com.miniIM.chatroom;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.miniIM.client.Client;

public class ChatRoom extends JFrame{
	
	private JTextArea receivedMessageTextArea;
	private JTextArea sendingMessageTextArea;
	private JList userList;
	private JScrollPane receivedMessageScrollPane;
	private JScrollPane sendingMessageScrollPane;
	private JScrollPane userListScrollPane;
	
	private JButton btnSend;
	private JButton btnLogout;
	
	class listenToServer extends Thread {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
		}
	}
	
	ChatRoom(Client client) {
		
		// set main ChatRoom window property
		this.setTitle("Chat Room (" + client.USERNAME + ")");
		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension screenSize = tk.getScreenSize();
		int width = 500;
		int height = 500;
		this.setBounds(((int)screenSize.getWidth()-width)/2, ((int)screenSize.getHeight()-height)/2, width, height);
		this.setResizable(false);
		this.setLayout(null);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		// layout control variables
		int column1X = 5;
		int row1Y = 5;
		int column1Width = 350;
		int row1Height = 350;
		int gap = 5;
		int merge = 10;
		int row2Y = row1Y + row1Height + gap;
		int column2X = column1X + column1Width + gap;
		
		// set receivedMessageTextArea property
		receivedMessageTextArea = new JTextArea("Welcome, " + client.USERNAME + ".");
		receivedMessageTextArea.setBounds(column1X, row1Y, column1Width, row1Height);
		receivedMessageTextArea.setLineWrap(true);
		receivedMessageTextArea.setWrapStyleWord(true);
		receivedMessageTextArea.setEditable(false);
		receivedMessageTextArea.setBackground(Color.WHITE);
		receivedMessageScrollPane = new JScrollPane(receivedMessageTextArea);
		receivedMessageScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		receivedMessageScrollPane.setBounds(column1X, row1Y, column1Width, row1Height);
		
		
		// set sendingMessageTextArea property
		sendingMessageTextArea = new JTextArea("Enter message HERE and click SEND to send it.");
		sendingMessageTextArea.setBounds(column1X, row2Y, column1Width, height-row2Y-34);
		sendingMessageTextArea.setLineWrap(true);
		sendingMessageTextArea.setWrapStyleWord(true);
		sendingMessageScrollPane = new JScrollPane(sendingMessageTextArea);
		sendingMessageScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		sendingMessageScrollPane.setBounds(column1X, row2Y, column1Width, height-row2Y-34);
		
		
		// set userList property
		userList = new JList();
		userList.setBounds(column2X, row1Y, width-column2X-merge, row1Height);
		userListScrollPane = new JScrollPane(userList, 
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		userListScrollPane.setBounds(column2X, row1Y, width-column2X-merge, row1Height);
		
		// set SEND Button property
		btnSend = new JButton("SEND");
		btnSend.setBounds(column2X+15, row2Y+20, width-column2X-40, 25);
		
		// set LOGOUT Button property
		btnLogout = new JButton("LOG OUT");
		btnLogout.setBounds(column2X+15, row2Y+65, width-column2X-40, 25);
		
		
		
		// TODO 
		
		
		
		
		// start a thread to use listenToServer to receive message from server when new a ChatRoom
		new Thread(new listenToServer()).start();
		
		this.add(receivedMessageScrollPane);
		this.add(sendingMessageScrollPane);
		this.add(userListScrollPane);
		this.add(btnSend);
		this.add(btnLogout);
		this.setVisible(true);
	}
	
	
	
	
	
	// use for test the frame of Chat Room
	public static void main(String[] args) {
		new ChatRoom(new Client("127.0.0.1", "123", "123"));
	}
	
}
