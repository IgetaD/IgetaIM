package igetaIm.server;

import javax.swing.JOptionPane;

public class LaunchServer {
	
	public static void main(String[] args) {
		
		try {
			Server server = new Server();
			server.setVisible(true);
			server.startSession();
		}
		catch(NullPointerException npe) {
			JOptionPane.showMessageDialog(null, "An error has occurred, please restart program.");
		}
		

	}//end main() method
}//end LaunchServer class

