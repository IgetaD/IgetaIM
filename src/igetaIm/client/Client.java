package igetaIm.client;

import javax.swing.JFrame;
import java.awt.Color;
import java.awt.Font;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Create the window for the server and all its connections
 * @author IgetaD
 *
 */
@SuppressWarnings("serial")
public class Client extends JFrame implements FocusListener {
	//instance variables to create GUI chat window
	private static final int WIDTH 				= 400;
	private static final int LENGTH 			= 500;
	private static final String DEFAULT_MESSAGE = "Type message here...";
	private String clientUsername 				= "Default";
	private final String PADDING 				= "    ";
	private final String END_MESSAGE 			= "82141b52d4a7cbbcb87a81515c443453a2d5";
	private final int PORT 						= 8080;
	private JPanel displayPanel;
	private JTextField textInputBox;
	private JTextArea textArea;
	private JScrollPane scrollPane;
	private JMenuBar menuBar;
	private JMenu fileList;

	private JMenu textList;
	private JMenu colorList;
	private JMenu sizeList;
	private JMenu editList;
	private JMenuItem sizeSmall;
	private JMenuItem sizeMedium;
	private JMenuItem sizeLarge;
	private JMenu systemList;
	private JMenuItem exitItem;
	//colors
	private JMenuItem itemCyan;
	private JMenuItem itemGreen;
	private JMenuItem itemMagenta;
	private JMenuItem itemRed;
	private JMenuItem itemPink;
	//instance variables for establishing connections
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;
	private Socket myConnection;
	private String serverIp;

	public Client(String host) {
		super("IgetaIM - Client");
		
		clientUsername = JOptionPane.showInputDialog("Enter a username:");
		clientUsername = clientUsername.trim();
		clientUsername = clientUsername.toUpperCase();
		
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
		textInputBox.setEditable(true);
		textInputBox.addFocusListener(this);
		
		//creates color list in Edit menu
		textList = new JMenu("Text");
		colorList = new JMenu("Color");
		itemCyan = new JMenuItem("Cyan");
		itemGreen = new JMenuItem("Green");
		itemMagenta = new JMenuItem("Magenta");
		itemRed = new JMenuItem("Red");
		itemPink = new JMenuItem("Pink");
		itemCyan.addActionListener(new MyListener());
		itemGreen.addActionListener(new MyListener());
		itemMagenta.addActionListener(new MyListener());
		itemRed.addActionListener(new MyListener());
		itemPink.addActionListener(new MyListener());
		textList.add(colorList);
		colorList.add(itemCyan);
		colorList.add(itemGreen);
		colorList.add(itemMagenta);
		colorList.add(itemRed);
		colorList.add(itemPink);
		//creates size menu under edit menu
		sizeList = new JMenu("Size");
		sizeSmall = new JMenuItem("Small");
		sizeMedium = new JMenuItem("Medium");
		sizeLarge = new JMenuItem("Large");
		sizeSmall.addActionListener(new MyListener());
		sizeMedium.addActionListener(new MyListener());
		sizeLarge.addActionListener(new MyListener());
		textList.add(sizeList);
		sizeList.add(sizeSmall);
		sizeList.add(sizeMedium);
		sizeList.add(sizeLarge);
		//trigger fires when the user hits enter on their keyboard
		textInputBox.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					String input = textInputBox.getText();
					input.trim();
					if(input.length() < 1) {
					//do nothing
					}
					else {
						DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm");
						String formattedTimeString = LocalDateTime.now().format(timeFormatter);
						
						transmitMessage(formattedTimeString + PADDING + clientUsername + ":  " + input);
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
		editList = new JMenu("Edit");
		systemList = new JMenu("System");
		exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(new MyListener());
		
		editList = new JMenu("Edit");
		editList.add(textList);
		systemList.add(exitItem);
		menuBar.add(fileList);
		menuBar.add(editList);
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
					System.exit(0);
				}
			}
			else if(actionCommand.equals("Cyan")) {
				textArea.setForeground(Color.CYAN);
			}
			else if(actionCommand.equals("Green")) {
				textArea.setForeground(Color.GREEN);
			}
			else if(actionCommand.equals("Magenta")) {
				textArea.setForeground(Color.MAGENTA);
			}
			else if(actionCommand.equals("Red")) {
				textArea.setForeground(Color.RED);
			}
			else if(actionCommand.equals("Pink")) {
				textArea.setForeground(Color.PINK);
			}
			else if(actionCommand.equals("Small")) {
				Font fontSmall = new Font(null, Font.PLAIN, 12);
				textArea.setFont(fontSmall);
			}
			else if(actionCommand.equals("Medium")) {
				Font fontMedium = new Font(null, Font.PLAIN, 14);
				textArea.setFont(fontMedium);
			}
			else if(actionCommand.equals("Large")) {
				Font fontLarge = new Font(null, Font.PLAIN, 16);
				textArea.setFont(fontLarge);
			}
		}//end actionPerformed() method
	}//end MyListener class
	
	/**
	 * Starts connections and streams
	 */
	public void startSession() {
		try {
			myConnection = new Socket(InetAddress.getByName(serverIp),PORT);
			outputStream = new ObjectOutputStream(myConnection.getOutputStream());
			outputStream.flush(); //cleans up leftover data
			inputStream = new ObjectInputStream(myConnection.getInputStream());
			
			if(myConnection != null && outputStream != null && inputStream != null) {
				chatSession();
			}	
		}
		catch(IOException ioe) {
			System.out.println("An error has occurred in client's startSession() method.");
		}
		finally {
			clientDisconnect();
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
			System.out.println("The client is terminating the chat session...");
		}
	}//end chatSession() method
	
	/**
	 * Closes streams and connections
	 */
	private void clientDisconnect() {
		transmitMessage(clientUsername + " has left the session.");
		
		try {
			outputStream.close();
			inputStream.close();
			myConnection.close();
		}
		catch(IOException ioe) {
			System.out.println("An error has occurred in client's clientDisconnect() method.");
		}
	}//end clientDisconnect() method
	
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
			System.out.println("The client was unable to transmit the message.");
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
	
}//end Client class
