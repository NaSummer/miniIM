package com.miniIM.login;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.miniIM.chatroom.ChatRoom;
import com.miniIM.client.Client;
import com.miniIM.register.Register;

public class Login extends JFrame{
	
	final static int PORT = Client.PORT;
	
	private JLabel JLServerAddress;
	private JLabel JLUserID;
	private JLabel JLUserPWD;
	private JTextField inputServerAddress; 
	private JTextField inputUserID;
	private JPasswordField inputUserPWD;
	private JButton btnLogin;
	private JButton btnRegister;
	
	private Login() {
		JLServerAddress = new JLabel("Server");
		JLUserID = new JLabel("Username");
		JLUserPWD = new JLabel("Password");
		inputServerAddress = new JTextField();
		inputUserID = new JTextField();
		inputUserPWD = new JPasswordField();
		btnLogin = new JButton("Login");
		btnRegister = new JButton("Register");
		
		/* set register window */
		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension screenSize = tk.getScreenSize();
		final int WIDTH = 265;
		final int HIGHT = 210;
		this.setBounds(((int)screenSize.getWidth()-WIDTH)/2, ((int)screenSize.getHeight()-HIGHT)/2, WIDTH, HIGHT);
		this.setResizable(false);
		this.setLayout(null);
		this.setTitle("Mini IM");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		final int COLUMN1_X = 30;
		final int BEGINNING_ROW_Y = 30;
		final int ROW_HEIGHT = 30;
		final int LABEL_HEIGHT = 20;
		final int LABEL_WIDTH_1 = 80;
		final int COLUMN2_X = COLUMN1_X + LABEL_WIDTH_1;
		final int LABEL_WIDTH_2 = WIDTH - (COLUMN1_X*2+LABEL_WIDTH_1+5);
		
		/* set JLable */
		JLServerAddress.setBounds(COLUMN1_X, BEGINNING_ROW_Y+ROW_HEIGHT*0, LABEL_WIDTH_1, LABEL_HEIGHT);
		JLUserID.setBounds(COLUMN1_X, BEGINNING_ROW_Y+ROW_HEIGHT*1, LABEL_WIDTH_1, LABEL_HEIGHT);
		JLUserPWD.setBounds(COLUMN1_X, BEGINNING_ROW_Y+ROW_HEIGHT*2, LABEL_WIDTH_1, LABEL_HEIGHT);
		
		/* set JTextField */
		inputServerAddress.setBounds(COLUMN2_X, BEGINNING_ROW_Y+ROW_HEIGHT*0, LABEL_WIDTH_2, LABEL_HEIGHT);
		inputUserID.setBounds(COLUMN2_X, BEGINNING_ROW_Y+ROW_HEIGHT*1, LABEL_WIDTH_2, LABEL_HEIGHT);
		inputUserPWD.setBounds(COLUMN2_X, BEGINNING_ROW_Y+ROW_HEIGHT*2, LABEL_WIDTH_2, LABEL_HEIGHT);
		inputUserPWD.setEchoChar('*');
		
		/* set JButton */ 
		final int BUTTON_WIGHT = 90;
		final int BUTTON_HEIGHT = 20;
//		final int BUTTON_Y = (BEGINNING_ROW_Y+ROW_HEIGHT*3) + (( HIGHT - (BEGINNING_ROW_Y+ROW_HEIGHT*3) )/2) - (BUTTON_HEIGHT/2);
		final int BUTTON_Y = 135;
		final int BUTTON_GAP = 20;
		final int BUTTON_COLUMN1_X = WIDTH/2 - BUTTON_GAP/2 - BUTTON_WIGHT - 3;
		final int BUTTON_COLUMN2_X = BUTTON_COLUMN1_X + BUTTON_WIGHT + BUTTON_GAP;
		btnLogin.setBounds(BUTTON_COLUMN1_X, BUTTON_Y, BUTTON_WIGHT, BUTTON_HEIGHT);
		btnRegister.setBounds(BUTTON_COLUMN2_X, BUTTON_Y, BUTTON_WIGHT, BUTTON_HEIGHT);
		
		/* add inputUserPWD Enter KeyListener */
		inputUserPWD.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode()==KeyEvent.VK_ENTER) {
					/* get input from JTextField */
					String serverAddress = inputServerAddress.getText();
					String username = inputUserID.getText();
					String password = new String(inputUserPWD.getPassword());
					
					tryLogin(serverAddress, username, password);
				}
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		/* add Login button Action Listener */
		btnLogin.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				/* get input from JTextField */
				String serverAddress = inputServerAddress.getText();
				String username = inputUserID.getText();
				String password = new String(inputUserPWD.getPassword());
				
				tryLogin(serverAddress, username, password);
			}
		});
		
		/* add Register button Action Listener*/
		btnRegister.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Login.this.dispose();
				new Register(Login.this);
			}
		});
		
		this.add(JLServerAddress);
		this.add(JLUserID);
		this.add(JLUserPWD);
		this.add(inputServerAddress);
		this.add(inputUserID);
		this.add(inputUserPWD);
		this.add(btnLogin);
		this.add(btnRegister);
		this.setVisible(true);
	}
	
	/*  */
	private void tryLogin(String serverAddress, String username, String password) {
		
		/* judge correctness of input */
		if (serverAddress.equals("")) {
			JOptionPane.showMessageDialog(null, JLServerAddress.getText()+" can't be blank.");
		} else if (username.equals("")) {
			JOptionPane.showMessageDialog(null, JLUserID.getText()+" can't be blank.");
		} else if (password.equals("")) {
			JOptionPane.showMessageDialog(null, JLUserPWD.getText()+" can't be blank.");
		} else {
			
			/* create new client */
			Client client = new Client(serverAddress, username, password);
			
			/* get the correctness of username and password from server */
			if (client.isLoggedIn()) {
				Login.this.dispose(); // close login window
				new ChatRoom(client).setVisible(true); // open new ChatRoom window
			} else {
				JOptionPane.showMessageDialog(null, "Cannot connect to server(" + serverAddress + ") or incorrect Username or Password.");
			}
		}
	}
	
	/*  */
	public void afterRegister(String serverAddress, String username) {
		inputServerAddress.setText(serverAddress);
		inputUserID.setText(username);
	}
	
	/*  */
	public static void main(String[] args) {
		new Login();
	}

}
