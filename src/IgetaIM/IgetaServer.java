package seesaw;

import javax.swing.JOptionPane;

public class IgetaServer {
	
	public static void main(String[] args) {
		
		try {
			ServerWindow server = new ServerWindow();
			server.setVisible(true);
			server.startSession();
		}
		catch(NullPointerException npe) {
			JOptionPane.showMessageDialog(null, "An error hass occurred, please restart program.");
		}
		

	}//end main() method
}//end MessengerApp class

