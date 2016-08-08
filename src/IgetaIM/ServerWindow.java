import javax.swing.JFrame;
import java.awt.Color;
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
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Create the window for the server and all its connections
 * @author IgetaD
 *
 */
@SuppressWarnings("serial")
public class ServerWindow extends JFrame implements FocusListener {
	
	//instance variables to create GUI chat window
	private static final int WIDTH 							= 400;
	private static final int LENGTH 						= 500;
	private static final String DEFAULT_MESSAGE 			= "Type message here...";
	private final String PADDING 							= "    ";
	private final String END_MESSAGE 						= "82141b52d4a7cbbcb87a81515c443453a2d5";
	private final String serverUsername 					= "SERVER";
	private final int PORT 									= 8214;
	private static ArrayList<ClientConnection> clientList 	= new ArrayList<>();
	private static LinkedList<String> messageQueue 			= new LinkedList<>();
	private JPanel displayPanel;
	private JTextField textInputBox;
	private JTextArea textArea;
	private JScrollPane scrollPane;
	private JMenuBar menuBar;
	private JMenu fileList;
	private JMenu viewList;
	private JMenu systemList;
	private JMenuItem endChatItem;
	private JMenuItem exitItem;
	
	//instance variables for establishing connections
	private ServerSocket serverSocket;
	private Socket clientSocket;
	
	/**
	 * Class that creates the server's window and components
	 */
	public ServerWindow() {
		super("IgetaIM - Server");

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
								clientList.get(i).transmitMessage(serverUsername + " has ended the session.");
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
//		textInputBox.setBackground(Color.GRAY); //sets the background color
//		textInputBox.setForeground(Color.GREEN);//sets the text typed color
//		textInputBox.setCaretColor(Color.GREEN);//sets the blinking cursor color
		textInputBox.setEditable(true);
//		textInputBox.addMouseListener(new MyMouseListener());
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
							synchronized(this) {
								for(int i = 0; i < clientList.size(); i++) {
									clientList.get(i).transmitMessage("(" + LocalTime.now() + ") " + serverUsername + ": " + input);
								}
							}//end synchronized
							appendMessage("(" + LocalTime.now() + ") " + serverUsername + ": " + input);
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
		viewList = new JMenu("View");
		systemList = new JMenu("System");
		//Creates JMenuItems to go inside JMenus
		endChatItem = new JMenuItem("End Session");
		exitItem = new JMenuItem("Exit");
		//assigns a listener to exitItem using an inner MyListener class
		exitItem.addActionListener(new MyListener());
		
		//adds JMenuItems to their respective JMenus
		fileList.add(endChatItem);
		systemList.add(exitItem);
		menuBar.add(fileList);
		menuBar.add(viewList);
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
								clientList.get(i).transmitMessage(serverUsername + " has ended the session.");
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
//	}//end MyMouseListener class
	
	/**
	 * Starts connections and streams
	 */
	public void startSession() {
		
		try {
			serverSocket = new ServerSocket(PORT, 10);
			appendMessage("Waiting for someone to connect...");
			
			while(true) {
				/*
				 * waits for a new socket to be returned by accept() method
				 * accept() method is typically placed in a loop
				 */
				clientSocket = serverSocket.accept();
				
				ClientConnection clientHandler = new ClientConnection(clientSocket);
				Thread myThread = new Thread(clientHandler);
				clientList.add(clientHandler);
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
			transmitMessage("You have joined " + serverUsername + "'s session.");

			try {
				do {
					//type cast the inputStream message to a string
					message = (String)inputStream.readObject();
					messageQueue.offer(message);
					broadcastMessage(message);
					
					if(!message.equals(END_MESSAGE)) {
						//appends message to server's chat window
						appendMessage(message);
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
			synchronized(this) {
				//loops through messageQueue
				for(int i = 0; i < messageQueue.size(); i++) {
					String s = "";
					s = messageQueue.poll();
					//broadcasts message to all clients
					for(int j = 0; j < clientList.size(); j++) {
						clientList.get(j).transmitMessage(s);
					}
				}//end for loop
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
	
}//end ServerWindow class
