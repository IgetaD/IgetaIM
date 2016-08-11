package igetaIm.client;

import javax.swing.JOptionPane;

public class LaunchClient {
	
	public static void main(String[] args) {
		
		try {
			Client client = new Client("192.168.1.124"); //or 127.0.0.1
			client.setVisible(true);
			client.startSession();
		}
		catch(NullPointerException npe) {
			JOptionPane.showMessageDialog(null, "There are currently no active servers to connect to.");
			System.exit(0);
		}
		
		
	}//end main() method
	
}//end LaunchClient class
