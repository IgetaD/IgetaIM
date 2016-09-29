package igetaIm.server;

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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.time.format.DateTimeFormatter;

/**
 * Create the window for the server and all its connections
 * @author IgetaD
 *
 */
@SuppressWarnings("serial")
public class Server extends JFrame implements FocusListener {
	
	//instance variables to create GUI chat window
	private final int WIDTH 								= 400;
	private final int LENGTH 								= 500;
	private final String DEFAULT_MESSAGE 					= "Type message here...";
	private final String PADDING 							= "    ";
	private final String END_MESSAGE 						= "82141b52d4a7cbbcb87a81515c443453a2d5";
	private final String SERVERNAME 						= "SERVER";
	private final int PORT 									= 8080;
	private static ArrayList<ClientConnection> clientList;
	private static LinkedList<String> messageQueue;
	
	//GUI components for JFrame
	private JPanel displayPanel;
	private JTextField textInputBox;
	private JTextArea textArea;
	private JScrollPane scrollPane;
	private JMenuBar menuBar;
	private JMenu fileList;
	private JMenu editList;
	private JMenu textList;
	private JMenu colorList;
	private JMenu sizeList;
	private JMenuItem saveItem;
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
	private ServerSocket serverSocket;
	private Socket clientSocket;
	
	/**
	 * Class that creates the server's window and components
	 */
	public Server() {
		super("IgetaIM - Server");
		clientList = new ArrayList<>();
		messageQueue = new LinkedList<>();
		//need to implement java.awt.FlowLayout
		this.setLayout(new BorderLayout());
		this.setSize(WIDTH,LENGTH);
		//do nothing since we have a WindowListener below
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			/**
			 * Prompts user for confirmation to end program
			 */
			@Override
			public void windowClosing(WindowEvent windowEvent) {
				int choice = 0;
				choice = JOptionPane.showConfirmDialog(null, "Are you sure you want to exit?", "Prompt", JOptionPane.YES_NO_OPTION);
				if(choice == 0) {
					//if a connection exists send message to clients before terminating
					if(clientSocket != null) {
						if(clientList.size() > 0) {
							for(int i = 0; i < clientList.size(); i++) {
								clientList.get(i).transmitMessage(SERVERNAME + " has ended the session.");
								clientList.get(i).transmitMessage(END_MESSAGE);
							}//and inner for loop
						}//end if statement
						System.exit(0);
					}
					else {
						//if no connections exist simply exit program
						System.exit(0);
					}
				}//end if statement
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
		//adds the component to the panel with a scrollPane
		displayPanel.add(scrollPane, BorderLayout.CENTER);
		
		//create the component where user will input text
		textInputBox = new JTextField(255);
		textInputBox.setText(DEFAULT_MESSAGE);
		textInputBox.setEditable(true);
		textInputBox.addFocusListener(this);
		textInputBox.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						String input = textInputBox.getText();
						input.trim();
						if(input.length() < 1) {
							//do nothing
						}
						else if(clientList.size() == 0) {
							appendMessage(input + "\n" + PADDING + "You currently have no active chat sessions.");
							textInputBox.setText("");
						}
						else {
							
							DateTimeFormatter timeFormatter2 = DateTimeFormatter.ofPattern("hh:mm");
							String formattedTimeString = LocalDateTime.now().format(timeFormatter2);
							
							synchronized(this) {
								messageQueue.offer(formattedTimeString + PADDING + SERVERNAME + ":  " + input);
								for(int i = 0; i < clientList.size(); i++) {
									clientList.get(i).transmitMessage(formattedTimeString + PADDING + SERVERNAME + ":  " + input);
								}
							}//end synchronized
							appendMessage(formattedTimeString + PADDING + SERVERNAME + ":  " + input);
							textInputBox.setText("");
						}

					}//end actionPerformed() method
				}//end ActionListener Object
		);//end anonymous inner class
		
		//adds the component to the panel
		displayPanel.add(textInputBox, BorderLayout.SOUTH);
		//adds the two panels created to the JFrame
		this.add(displayPanel);
		
		//JMenuBar is a container for JMenu
		menuBar = new JMenuBar();
		//Creates JMenu items to go inside JMenuBar
		fileList = new JMenu("File");
		saveItem = new JMenuItem("Save");
		saveItem.addActionListener(new MyListener());
		fileList.add(saveItem);
		editList = new JMenu("Edit");
		
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

		systemList = new JMenu("System");
		//Creates JMenuItems to go inside JMenus
		exitItem = new JMenuItem("Exit");
		//assigns a listener to exitItem using an inner MyListener class
		exitItem.addActionListener(new MyListener());
		
		//adds JMenuItems to their respective JMenus
		editList.add(textList);
		
		systemList.add(exitItem);
		menuBar.add(fileList);
		menuBar.add(editList);
		menuBar.add(systemList);
		//adds the menuBar to the JFrame
		this.setJMenuBar(menuBar);
	}
	
	/**
	 * Clears the textInputBox when user clicks in the field
	 */
	@Override
	public void focusGained(FocusEvent e) {
		textInputBox.setText("");
    }
	
	/**
	 * Sets textInputBox to default message when focus is lost in field
	 */
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
					//if a connection exists send message to clients before terminating
					if(clientSocket != null) {
						if(clientList.size() > 0) {
							for(int i = 0; i < clientList.size(); i++) {
								clientList.get(i).transmitMessage(SERVERNAME + " has ended the session.");
								clientList.get(i).transmitMessage(END_MESSAGE);
							}//and inner for loop
						}//end if statement
						System.exit(0);
					}
					else {
						//if no connections exist simply exit program
						System.exit(0);
					}
				}
			}//end if
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
			else if(actionCommand.equals("Save")) {
				writeToFile();
			}
		}//end actionPerformed() method
	}//end MyListener class
	
	/**
	 * Starts connections and streams
	 */
	public void startSession() {
		
		boolean listening = true;
		
		try {
			serverSocket = new ServerSocket(PORT, 10);
			appendMessage("Waiting for someone to connect...");
			
			while(listening) {
				/*
				 * waits for a new socket to be returned by accept() method
				 * accept() method is typically placed in a loop
				 */
				clientSocket = serverSocket.accept();
				
				ClientConnection clientHandler = new ClientConnection(clientSocket);
				Thread myThread = new Thread(clientHandler);
				
				synchronized(this) {
					clientList.add(clientHandler);
				}
				myThread.start();
			}//end while loop
		}//end try statement
		catch(IOException ioe) {
			JOptionPane.showMessageDialog(null, "The server program is already running.");
			System.exit(0);
		}
	}//end startSession() method
	
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
	
	/**
	 * Writes contents of conversation to a text file
	 */
	private void writeToFile() {
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
		String formattedDateString = LocalDateTime.now().format(dateFormatter);
		
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh.mm.ss");
		String formattedTimeString = LocalDateTime.now().format(timeFormatter);
		
		String fileName 			= formattedDateString + "_" + formattedTimeString + "_IgetaIM";
		File myFile 				= null;
		PrintWriter outputStream 	= null;
		
		try {
			myFile = new File(fileName);
			outputStream = new PrintWriter(new FileOutputStream(myFile));
			
			synchronized(this) {
				//loops through messageQueue
				for(int i = 0; i < messageQueue.size(); i++) {
					//print a newline if not the last message
					outputStream.println(messageQueue.get(i));
				}
			}//end sync
		}//end try statement
		catch(FileNotFoundException fnf) {
			System.out.println("Error: File not found!");
			System.exit(1);

		}
		catch(NullPointerException npe) {
			System.out.println("Error: Null pointer exception!");
			System.exit(1);
		}
		catch(NoSuchElementException nse) {
			System.out.println("Error: NoSuchElement exception!");
			System.exit(1);
		}
		finally {
			outputStream.close();
			JOptionPane.showMessageDialog(null, "Conversation has been saved!");
		}
	}//end writeToFile() method
	
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Each client's communication is handled by a different thread
	 * @author IgetaD
	 *
	 */
	private class ClientConnection extends Thread {
		private Socket clientSocket;
		private ObjectOutputStream outputStream;
		private ObjectInputStream inputStream;
		
		/**
		 * ClientConnection constructor
		 * @param clientSocket
		 */
		private ClientConnection(Socket clientSocket) {
			this.clientSocket = clientSocket;
		}
		
		@Override
		public void run() {
			
			try {
				while(true) {
					//creates stream to send messages to client
					outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
					outputStream.flush();
					//creates stream to receive messages from client
					inputStream = new ObjectInputStream(clientSocket.getInputStream());
					chatSession();
				}//end while loop
			}//end try statement
			catch(IOException ioe) {
				//do nothing
			}
			finally {
				disconnectClient();
			}
		}//end run() method
		
		/**
		 * Transmits messages to display area while a connection is established
		 * @throws IOException
		 */
		private void chatSession() throws IOException {
			String message = "";
			//tell the client they have connected to the server's session
			transmitMessage("You have joined " + SERVERNAME + "'s session.");

			try {
				do {
					synchronized(this) {
						//type cast the inputStream message to a string
						message = (String)inputStream.readObject();
						messageQueue.offer(message);
						broadcastMessage(message);
						
						if(!message.equals(END_MESSAGE)) {
							//appends message to server's chat window
							appendMessage(message);
						}
					}
				} while(!message.equals(END_MESSAGE));
			}
			catch (ClassNotFoundException cnf) {
				System.out.println("An error has occurred in server chatSession() method!");
			}
		}//end chatSession() method
		
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
				System.out.println("The server's message was unable to be transmitted.");
			}
		}//end transmitMessage() method
		
		private void broadcastMessage(String message) {
			//broadcasts message to all clients
			for(int j = 0; j < clientList.size(); j++) {
				clientList.get(j).transmitMessage(message);
			}
		}//end broadcastMessage() method
		
		/**
		 * Closes streams and connections
		 */
		private void disconnectClient() {

			try {
				outputStream.close();
				inputStream.close();
				clientSocket.close();
				scrubClientList();
				if(clientList.size() == 0) {
					appendMessage("Waiting for someone to connect...");
				}
			}
			catch(IOException ioe) {
				System.out.println("An error has occurred in the server's disconnect() method.");
			}
			catch(IndexOutOfBoundsException out) {
				System.out.println("Index out of range.");
			}
		}//end disconnect() method
		
		/**
		 * Goes through clientList to remove disconnected sockets
		 */
		private void scrubClientList() {
			for(int i = 0; i < clientList.size(); i++) {
				if(clientList.get(i).clientSocket.isClosed()) {
					clientList.remove(i);
				}
			}//end for loop
		}//end scrubClientList method
		
	}//end ClientConnection class
	
}//end Server class
