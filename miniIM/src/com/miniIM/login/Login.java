package com.miniIM.login;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.miniIM.client.Client;
import com.miniIM.register.Register;

public class Login extends JFrame{
	
	final static int PORT = 23333;
	
	private JLabel JLServerAddress;
	private JLabel JLUserID;
	private JLabel JLUserPWD;
	private JTextField JTFServerAddress;
	private JTextField JTFUserID;
	private JPasswordField JPFUserPWD;
	private JButton btnLogin;
	private JButton btnRegister;
	
	private Login() {
		JLServerAddress = new JLabel("Server");
		JLUserID = new JLabel("Username");
		JLUserPWD = new JLabel("Password");
		JTFServerAddress = new JTextField();
		JTFUserID = new JTextField();
		JPFUserPWD = new JPasswordField();
		btnLogin = new JButton("Login");
		btnRegister = new JButton("Register");
		
		// set register window
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
		// set JLable
		JLServerAddress.setBounds(COLUMN1_X, BEGINNING_ROW_Y+ROW_HEIGHT*0, LABEL_WIDTH_1, LABEL_HEIGHT);
		JLUserID.setBounds(COLUMN1_X, BEGINNING_ROW_Y+ROW_HEIGHT*1, LABEL_WIDTH_1, LABEL_HEIGHT);
		JLUserPWD.setBounds(COLUMN1_X, BEGINNING_ROW_Y+ROW_HEIGHT*2, LABEL_WIDTH_1, LABEL_HEIGHT);
		// set JTextField
		JTFServerAddress.setBounds(COLUMN2_X, BEGINNING_ROW_Y+ROW_HEIGHT*0, LABEL_WIDTH_2, LABEL_HEIGHT);
		JTFUserID.setBounds(COLUMN2_X, BEGINNING_ROW_Y+ROW_HEIGHT*1, LABEL_WIDTH_2, LABEL_HEIGHT);
		JPFUserPWD.setBounds(COLUMN2_X, BEGINNING_ROW_Y+ROW_HEIGHT*2, LABEL_WIDTH_2, LABEL_HEIGHT);
		JPFUserPWD.setEchoChar('*');
		// set JButton 
		final int BUTTON_WIGHT = 90;
		final int BUTTON_HEIGHT = 20;
//		final int BUTTON_Y = (BEGINNING_ROW_Y+ROW_HEIGHT*3) + (( HIGHT - (BEGINNING_ROW_Y+ROW_HEIGHT*3) )/2) - (BUTTON_HEIGHT/2);
		final int BUTTON_Y = 135;
		final int BUTTON_GAP = 20;
		final int BUTTON_COLUMN1_X = WIDTH/2 - BUTTON_GAP/2 - BUTTON_WIGHT - 3;
		final int BUTTON_COLUMN2_X = BUTTON_COLUMN1_X + BUTTON_WIGHT + BUTTON_GAP;
		btnLogin.setBounds(BUTTON_COLUMN1_X, BUTTON_Y, BUTTON_WIGHT, BUTTON_HEIGHT);
		btnRegister.setBounds(BUTTON_COLUMN2_X, BUTTON_Y, BUTTON_WIGHT, BUTTON_HEIGHT);
		
		// add Login button Action Listener
		btnLogin.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				// TODO Auto-generated method stub
				Client client = new Client(serverAddress, username, password);
			}
		});
		
		// add Register button Action Listener
		btnRegister.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new Register();
			}
		});
		
		this.add(JLServerAddress);
		this.add(JLUserID);
		this.add(JLUserPWD);
		this.add(JTFServerAddress);
		this.add(JTFUserID);
		this.add(JPFUserPWD);
		this.add(btnLogin);
		this.add(btnRegister);
		this.setVisible(true);
	}
	
	
	public static void main(String[] args) {
		new Login();
	}

}
