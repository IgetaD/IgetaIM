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

/**
 * Create the window for the server and all its connections
 * @author IgetaD
 *
 */
@SuppressWarnings("serial")
public class ServerWindow extends JFrame implements FocusListener {
	
	//instance variables to create GUI chat window
	private static final int WIDTH = 400;
	private static final int LENGTH = 500;
	private static final String DEFAULT_MESSAGE = "Type message here...";
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
	private String serverUsername = "SERVER";
	private final String PADDING = "    ";
	private final String END_MESSAGE = "82141b52d4a7cbbcb87a81515c443453a2d5";
	
	//instance variables for establishing connections
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;
	private ServerSocket myServerSocket;
	private Socket mySocket;
	private final int PORT = 8214;
	
	public ServerWindow() {
		super("IgetaIM - Server");

		//need to implement java.awt.FlowLayout
		this.setLayout(new BorderLayout());
		this.setSize(WIDTH,LENGTH);
		//do nothing since we have a WindowListener below
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			/**
			 * Asks user if they wish to close the program
			 */
			@Override
			public void windowClosing(WindowEvent windowEvent) {
				int choice = 0;
				choice = JOptionPane.showConfirmDialog(null, "Are you sure you want to exit?", "Prompt", JOptionPane.YES_NO_OPTION);
				if(choice == 0) {
					transmitMessage(serverUsername + " has ended the session.");
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
		textInputBox.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						String input = textInputBox.getText();
						input.trim();
						if(input.length() < 1) {
						//do nothing
						}
						else if(mySocket == null) {
							appendMessage(input + "\n" + PADDING + "You currently have no active chat sessions.");
						}
						else {
							transmitMessage("(" + LocalTime.now() + ") " + serverUsername + " - " + input);
							appendMessage("(" + LocalTime.now() + ") " + serverUsername + " - " + input);
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
		viewList = new JMenu("View");
		endChatItem = new JMenuItem("End Session");
		systemList = new JMenu("System");
		exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(new MyListener());
		fileList.add(endChatItem);
		endChatItem.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					transmitMessage(serverUsername + " has ended the session.");
					transmitMessage(END_MESSAGE);
				}
			}
		);
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
					transmitMessage(serverUsername + " has ended the session.");
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
//	}//end MyMouseListener class
	
	/**
	 * Starts connections and streams
	 */
	public void startSession() {
		
		try {
			while(true) {
				myServerSocket = new ServerSocket(PORT, 10);
				appendMessage("Waiting for someone to connect...");
				mySocket = myServerSocket.accept();
				
				outputStream = new ObjectOutputStream(mySocket.getOutputStream());
				outputStream.flush(); //cleans up leftover data
				inputStream = new ObjectInputStream(mySocket.getInputStream());
				
				Runnable listen = new clientListener(mySocket);
				Thread thread1 = new Thread(listen);
				thread1.start();
				
				chatSession();
			}//end while loop
		}//end try statement
		catch(IOException ioe) {
			System.out.println("An error occurred in the startSession() method of the server.");
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
		String message = "";
		//tell the client they have connected to the server's session
		transmitMessage("You have joined " + serverUsername + "'s session.");
		
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
			System.out.println("An error has occurred in server chatSession() method!");
		}
		finally {
			System.out.println("Exiting chat session...");
		}
	}//end chatSession() method
	
	/**
	 * Closes streams and connections
	 */
	private void cleanUp() {
		System.out.println("Server clean up.");
		try {
			outputStream.close();
			inputStream.close();
			myServerSocket.close();
			mySocket.close();
		}
		catch(IOException ioe) {
			System.out.println("An error has occurred in the server's cleanUp() method.");
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
			System.out.println("An error has occurred in the server's transmitMessage() method.");
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
	
	private class clientListener extends Thread {
		private Socket mySocket;
		
		private clientListener(Socket mySocket) {
			this.mySocket = mySocket;
		}
		
		@Override
		public void run() {
			
			try {
				while(true) {
					mySocket = myServerSocket.accept();
					
					outputStream = new ObjectOutputStream(mySocket.getOutputStream());
					outputStream.flush(); //cleans up leftover data
					inputStream = new ObjectInputStream(mySocket.getInputStream());
					chatSession();
				}//end while loop
			}//end try statement
			catch(IOException ioe) {
				System.out.println("An error occurred in the startSession() method of the server.");
			}
			finally {
				cleanUp();
			}
		}
	}
}//end ChatWindow class
