import java.io.IOException;

// TODO: Auto-generated Javadoc
/**
 * The Class Main.
 */
public class Main {
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws IOException{
		// Get port
		int port = Integer.decode(args[0]).intValue();
		//choose thread-per-client or reactor
		MultipleClientProtocolServer server = new MultipleClientProtocolServer(port, new GameProtocolFactory());
		Thread serverThread = new Thread(server);
		serverThread.start();
		try {
			serverThread.join();
		}
		catch (InterruptedException e){
			System.out.println("Server stopped");
		}
	}
}
