package com.miniIM.chatroom;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.miniIM.client.Client;

public class ChatRoom extends JFrame{
	
	private Client client;
	
	private JTextArea receivedMessageTextArea;
	private JTextArea sendingMessageTextArea;
	private JList userList;
	private JScrollPane receivedMessageScrollPane;
	private JScrollPane sendingMessageScrollPane;
	private JScrollPane userListScrollPane;
	
	private JButton btnSend;
	private JButton btnLogout;
	
	private Vector users;
	
	public ChatRoom(Client client) {
		this.client = client;
		
		/* set main ChatRoom window property */
		this.setTitle("Chat Room (" + this.client.USERNAME + ")");
		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension screenSize = tk.getScreenSize();
		int width = 500;
		int height = 500;
		this.setBounds(((int)screenSize.getWidth()-width)/2, ((int)screenSize.getHeight()-height)/2, width, height);
		this.setResizable(false);
		this.setLayout(null);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		/* layout control variables */
		int column1X = 5;
		int row1Y = 5;
		int column1Width = 350;
		int row1Height = 350;
		int gap = 5;
		int merge = 10;
		int row2Y = row1Y + row1Height + gap;
		int column2X = column1X + column1Width + gap;
		
		/* set receivedMessageTextArea property */
		receivedMessageTextArea = new JTextArea("Welcome, " + client.USERNAME + ".\n");
		receivedMessageTextArea.setBounds(column1X, row1Y, column1Width, row1Height);
		receivedMessageTextArea.setLineWrap(true);
		receivedMessageTextArea.setWrapStyleWord(true);
		receivedMessageTextArea.setEditable(false);
		receivedMessageTextArea.setBackground(Color.WHITE);
		receivedMessageScrollPane = new JScrollPane(receivedMessageTextArea);
		receivedMessageScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		receivedMessageScrollPane.setBounds(column1X, row1Y, column1Width, row1Height);
		
		
		/* set sendingMessageTextArea property */
		sendingMessageTextArea = new JTextArea("Enter message HERE and click SEND to send it.");
		sendingMessageTextArea.setBounds(column1X, row2Y, column1Width, height-row2Y-34);
		sendingMessageTextArea.setLineWrap(true);
		sendingMessageTextArea.setWrapStyleWord(true);
		sendingMessageScrollPane = new JScrollPane(sendingMessageTextArea);
		sendingMessageScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		sendingMessageScrollPane.setBounds(column1X, row2Y, column1Width, height-row2Y-34);
		
		
		users = new Vector();
		/* set userList property */
		userList = new JList();
		userList.setBounds(column2X, row1Y, width-column2X-merge, row1Height);
		userListScrollPane = new JScrollPane(userList, 
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		userListScrollPane.setBounds(column2X, row1Y, width-column2X-merge, row1Height);
		
		/* set SEND Button property */
		btnSend = new JButton("SEND");
		btnSend.setBounds(column2X+15, row2Y+20, width-column2X-40, 25);
		
		/* set LOGOUT Button property */
		btnLogout = new JButton("LOG OUT");
		btnLogout.setBounds(column2X+15, row2Y+65, width-column2X-40, 25);
		
		
		
		/* SEND Button ClickListener */
		btnSend.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				client.sendMessage(sendingMessageTextArea.getText());
				sendingMessageTextArea.setText("");
			}
		});
		
		
		/* Ctrl+EnterKey Listener */
		sendingMessageTextArea.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent arg0) {
				
			}
			
			/* ctrl + enter */
			@Override
			public void keyReleased(KeyEvent arg0) {
				if ( (arg0.isControlDown()) && (arg0.getKeyCode()== KeyEvent.VK_ENTER) ) {
					client.sendMessage(sendingMessageTextArea.getText());
					sendingMessageTextArea.setText("");
				}	
			}
			
			@Override
			public void keyPressed(KeyEvent arg0) {
				
			}
		});
		
		/* LOGOUT Button ClickListener */
		btnLogout.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Send Log out Packet
				System.exit(EXIT_ON_CLOSE);
			}
		});
		
		
		/* start a thread to use listenToServer to receive message from server when new a ChatRoom */
		new Thread(new UserListListener()).start();
		new Thread(new MessagesListener()).start();
		
		this.add(receivedMessageScrollPane);
		this.add(sendingMessageScrollPane);
		this.add(userListScrollPane);
		this.add(btnSend);
		this.add(btnLogout);
	}
	
//	/* send message */
//	private void sendMessage() {
//		
//	}
	
	/* User list listener */
	class UserListListener extends Thread {
		@Override
		public void run() {
			
			/* add self first */
			users.add(client.USERNAME);
			userList.setListData(users);
			
			while (true) {
				String[][] clientUserList = client.getUserList();
				for (int i = 0; i < clientUserList.length; i++) {
					if (!users.contains(clientUserList[i][0])) {
						users.add(clientUserList[i][0]);
						userList.setListData(users);
						receivedMessageTextArea.append("User [" + clientUserList[i][0] + "] is online.\n");
					}
				}
				try {
					UserListListener.sleep(1000);
				} catch (InterruptedException e) {
					System.err.println("[UserListListener] Fail to Sleep UserListListener");
					e.printStackTrace();
				}
			}
		}
	}
	
	class MessagesListener extends Thread {
		@Override
		public void run() {
			while (true) {
				while (!client.messageList.isEmpty()) {
					String[] message = client.messageList.get(0).split(Long.toString(client.DEVICE_ID));
					receivedMessageTextArea.append("[" + message[0] + "] said:\n  -> " + message[1] + "\n\n");
					client.messageList.remove(0);
				}
				try {
					MessagesListener.sleep(500);
				} catch (InterruptedException e) {
					System.err.println("[MessagesListener] Fail to sleep MessagesListener");
					e.printStackTrace();
				}
			}
		}
	}
	
}
