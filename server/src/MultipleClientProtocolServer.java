import java.io.*;
import java.net.*;
import java.nio.channels.ServerSocketChannel;

/**
 * The Class MultipleClientProtocolServer.
 */
public class MultipleClientProtocolServer implements Runnable {
	
	/** The server socket. */
	private ServerSocket serverSocket;
	
	/** The server socket channel. */
	private ServerSocketChannel serverSocketChannel;
	
	/** The listen port. */
	private int listenPort;
	
	/** The factory. */
	private GameProtocolFactory factory;
	
	/**
	 * Instantiates a new multiple client protocol server.
	 *
	 * @param port the port
	 * @param p the protocol factory method
	 */
	public MultipleClientProtocolServer(int port, GameProtocolFactory p){
		listenPort = port;
		factory = p;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run(){
		try {
			serverSocketChannel = serverSocketChannel.open();
			serverSocket = serverSocketChannel.socket();
			InetSocketAddress iSA = new InetSocketAddress(listenPort);
			serverSocket.bind(iSA);
			System.out.println("Listening...");
			GameBoard gameBoard = GameBoard.getInstance();
			gameBoard.addGame(new Bluffer());
		}
		catch (IOException e) {
			System.out.println("Cannot listen on port " + listenPort);
		}
		while (true){
			try {
				ConnectionHandler newConnection = new ConnectionHandler(serverSocketChannel.accept(), factory.create());
				new Thread(newConnection).start();
			}
			catch (IOException e){
				System.out.println("Failed to accept on port " + listenPort);
			}
		}
	}

	/**
	 * Closes the connection
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void close() throws IOException{
		serverSocket.close();
	}
}
