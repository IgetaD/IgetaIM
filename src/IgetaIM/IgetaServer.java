import javax.swing.JOptionPane;

public class IgetaServer {
	
	public static void main(String[] args) {
		
		try {
			ServerWindow server = new ServerWindow();
			server.setVisible(true);
			server.startSession();
		}
		catch(NullPointerException npe) {
			JOptionPane.showMessageDialog(null, "An error has occurred, please restart program.");
		}
		
	}//end main() method
}//end IgetaServer class

