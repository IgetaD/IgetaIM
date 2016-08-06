package seesaw;

import javax.swing.JFrame;
import java.awt.Color;
//import java.awt.Dimension;
import java.awt.BorderLayout;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import java.io.*;
import java.net.*;
import java.awt.event.*;
import javax.swing.*;
import java.time.LocalTime;

/**
 * Create the window for the server and all its connections
 * @author IgetaD
 *
 */
@SuppressWarnings("serial")
public class ClientWindow extends JFrame implements FocusListener {
	//instance variables to create GUI chat window
	private static final int WIDTH = 400;
	private static final int LENGTH = 500;
	private static final String DEFAULT_MESSAGE = "Type message here...";
	private JPanel displayPanel;
	private JTextField textInputBox;
	private JTextArea textArea;
	private JScrollPane scrollPane;
	private String clientUsername = "test";
	private final String PADDING = "    ";
	//items for menu
	private JMenuBar menuBar;
	private JMenu viewList;
	private JMenu fileList;
	private JMenu systemList;
	private JMenuItem exitItem;
	private JMenuItem endSessionItem;
	private final String END_MESSAGE = "82141b52d4a7cbbcb87a81515c443453a2d5";
	
	//instance variables for establishing connections
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;
	private Socket myConnection;
	private String serverIp;
	private final int PORT = 8214;
	
	public ClientWindow(String host) {
		super("IgetaIM - Client");
		clientUsername = JOptionPane.showInputDialog("Enter a username:");
		serverIp = host;
		this.setLayout(new BorderLayout()); //need to implement java.awt.FlowLayout
		this.setSize(WIDTH,LENGTH);
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); //do nothing since we have a WindowListener below
		this.addWindowListener(new WindowAdapter() {
			/**
			 * Asks user if they wish to close the program
			 */
			@Override
			public void windowClosing(WindowEvent windowEvent) {
				int choice = 0;
				choice = JOptionPane.showConfirmDialog(null, "Are you sure you want to exit?", "Prompt", JOptionPane.YES_NO_OPTION);
				if(choice == 0) {
					transmitMessage(clientUsername + " has left the session.");
					transmitMessage(END_MESSAGE);
					System.exit(0);
				}
				else {
					//do nothing
				}
			}//end windowClosing() method
		});//end WindowAdapter anonymous class
		
		//create the panel that displays messages
		displayPanel = new JPanel();
		displayPanel.setLayout(new BorderLayout());
		displayPanel.setBackground(Color.BLACK);
		
		//create the component that will display messages
		textArea = new JTextArea();
		textArea.setEditable(false);			//ensure user doesn't edit content inside
		textArea.setForeground(Color.GREEN);	//set text color 
		textArea.setBackground(Color.DARK_GRAY);//set background color
		scrollPane = new JScrollPane(textArea);
		displayPanel.add(scrollPane, BorderLayout.CENTER);
		
		//create the component where user will input text
		textInputBox = new JTextField(255);
		textInputBox.setText(DEFAULT_MESSAGE);
//		textInputBox.setBackground(Color.GRAY); //sets the background color
//		textInputBox.setForeground(Color.GREEN);//sets the text typed color
//		textInputBox.setCaretColor(Color.GREEN);//sets the blinking cursor color
		textInputBox.setEditable(true);
//		textInputBox.addMouseListener(new MyMouseListener());
		textInputBox.addFocusListener(this);
		
		//trigger fires when the user hits enter on their keyboard
		textInputBox.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					String input = textInputBox.getText();
					input.trim();
					if(input.length() < 1) {
					//do nothing
					}
					else if(myConnection == null) {
						appendMessage(input + "\n" + PADDING + "You currently have no active chat sessions.");
					}
					else {
						transmitMessage("(" + LocalTime.now() + ") " + clientUsername + " - " + input);
						appendMessage("(" + LocalTime.now() + ") " + clientUsername + " - " + input);
						textInputBox.setText("");
						
					}
				}//end actionPerformed() method
			}//end ActionListener Object
		);//end anonymous inner class
		
		displayPanel.add(textInputBox, BorderLayout.SOUTH);
		
		//adds the two panels created to the JFrame
		this.add(displayPanel);
		
		//JMenuBar is a container for JMenu
		menuBar = new JMenuBar();
		//JMenu is a container for JMenuItems
		fileList = new JMenu("File");
		endSessionItem = new JMenuItem("End Session");
		
		endSessionItem.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					transmitMessage(clientUsername + " has left the session.");
					appendMessage("You have left the session.");
					transmitMessage(END_MESSAGE);
					cleanUp();
				}//end actionPerformed() method
			}//end ActionListener
		);
		
		systemList = new JMenu("System");
		exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(new MyListener());
		
		viewList = new JMenu("View");
		
		fileList.add(endSessionItem);
		systemList.add(exitItem);
		menuBar.add(fileList);
		menuBar.add(viewList);
		menuBar.add(systemList);
		setJMenuBar(menuBar);
	}
	
	@Override
	public void focusGained(FocusEvent e) {
		textInputBox.setText("");
    }
	
	@Override
    public void focusLost(FocusEvent e) {
        textInputBox.setText(DEFAULT_MESSAGE);
    }
	
	/**
	 * Private inner class created for button listeners
	 * @author IgetaD
	 *
	 */
	private class MyListener implements ActionListener {
		
		public void actionPerformed(ActionEvent event) {
			
			String actionCommand = event.getActionCommand();
			
			//when the exit button in the menu is clicked
			if(actionCommand.equals("Exit")) {
				int choice = 0;
				choice = JOptionPane.showConfirmDialog(null, "Are you sure you want to exit?", "Prompt", JOptionPane.YES_NO_OPTION);
				if(choice == 0) {
					transmitMessage(clientUsername + " has left the session.");
					transmitMessage(END_MESSAGE);
					System.exit(0);
				}
			}
		}//end actionPerformed() method
	}//end MyListener class
	
	/**
	 * Private inner class for mouse listeners
	 * @author IgetaD
	 *
	 */
//	private class MyMouseListener implements MouseListener {
//		
//		@Override
//		public void mouseReleased(MouseEvent mEvent) {
//			//do nothing
//		}
//		
//		@Override
//		public void mousePressed(MouseEvent mEvent) {
//			if(textInputBox.getText().equals(DEFAULT_MESSAGE)) {
//				textInputBox.setText("");
//			}
//			else {
//				//do nothing
//			}
//		}
//		
//		@Override
//		public void mouseExited(MouseEvent mEvent) {
//			//do nothing
//		}
//		
//		@Override
//		public void mouseEntered(MouseEvent mEvent) {
//			//do nothing
//		}
//		
//		@Override
//		public void mouseClicked(MouseEvent mEvent) {
//			//do nothing
//		}
//	}
	
	/**
	 * Starts connections and streams
	 */
	public void startSession() {
		try {
			myConnection = new Socket(InetAddress.getByName(serverIp),PORT);
			
			outputStream = new ObjectOutputStream(myConnection.getOutputStream());
			outputStream.flush(); //cleans up leftover data
			inputStream = new ObjectInputStream(myConnection.getInputStream());
			chatSession();
		}
		catch(IOException ioe) {
			System.out.println("An error has occurred in client's startSession() method.");
		}
		finally {
			cleanUp();
		}
	}//end startSession() method
	
	/**
	 * Transmits messages to display area while a connection is established
	 * @throws IOException
	 */
	private void chatSession() throws IOException {
		//tell the server who has connected to their session
		transmitMessage(clientUsername + " has joined the session.");
		String message = "";
		
		try {
			do {
				//type cast the inputStream message to a string
				message = (String)inputStream.readObject();
				if(!message.equals(END_MESSAGE)) {
					appendMessage(message);
				}
			} while(!message.equals(END_MESSAGE));
		}
		catch (ClassNotFoundException cnf) {
			System.out.println("An error has occurred in client's chatSession() method.");
		}
	}//end chatSession() method
	
	/**
	 * Closes streams and connections
	 */
	private void cleanUp() {
		transmitMessage(clientUsername + " has left the session.");
		
		try {
			outputStream.close();
			inputStream.close();
			myConnection.close();//closes the sockets between the computers
		}
		catch(IOException ioe) {
			System.out.println("An error has occurred in client's cleanUp() method.");
		}
	}//end cleanUp() method
	
	/**
	 * Transmits message and flushes extra data
	 * @param message
	 * @throws IOException
	 */
	private void transmitMessage(String message) {
		try {
			outputStream.writeObject(message);
			outputStream.flush();
		}
		catch(IOException ioe) {
			System.out.println("An error has occurred in the transmitMessage() method.");
		}
	}//end transmitMessage() method
	
	/**
	 * Displays a message to the textArea box
	 * @param string
	 */
	private void appendMessage(final String MESSAGE) {
		//creates a thread to append new messages
		SwingUtilities.invokeLater(
			new Runnable() {
				@Override
				public void run() {
					textArea.append("\n" + PADDING + MESSAGE + "\n");
				}//end run() method
			}//end anonymous inner class Runnable
		);
	}//end appendMessage() method
	
}//end ChatWindow class
