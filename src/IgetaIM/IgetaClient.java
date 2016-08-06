package seesaw;

import javax.swing.JOptionPane;

public class IgetaClient {
	
	public static void main(String[] args) {
		
		try {
			ClientWindow client = new ClientWindow("127.0.0.1"); //or 127.0.0.1
			client.setVisible(true);
			client.startSession();
		}
		catch(NullPointerException npe) {
			JOptionPane.showMessageDialog(null, "There are currently no active servers to connect to.");
			System.exit(0);
		}
		
		
	}//end main() method
	
}//end ClientTest class
