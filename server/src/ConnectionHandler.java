import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * Handles messages from clients.
 */
class ConnectionHandler implements Runnable {
	
	/** The buffer input. */
	private BufferedReader in;
	
	/** The buffer out. */
	private PrintWriter out;
	
	/** The client socket. */
	private SocketChannel clientSocket;
	
	/** The TBGP protocol. */
	private GameProtocol protocol;
	
	/** The tokenizer. */
	private FixedSeparatorMessageTokenizer tokenizer;
	
	/** The callback. */
	private ProtocolCallback<String> callback;
	
	/**
	 * Instantiates a new connection handler.
	 *
	 * @param socket the socket
	 * @param p the protocol
	 */
	public ConnectionHandler(SocketChannel socket, GameProtocol p)
	{
		in = null;
		out = null;
		clientSocket = socket;
		protocol = p;
		tokenizer = FixedSeparatorMessageTokenizer.create("\n",Charset.forName("UTF-8"));
		callback = new ProtocolCallback<String>() {

            public void sendMessage(String msg) throws IOException {
                if(msg != null){
                    ByteBuffer bytes = tokenizer.getBytesForMessage(new String(msg.toString()));
                    clientSocket.write(bytes);
                }
            }
        };
		System.out.println("Accepted connection from client!");
		System.out.println("The client is from: " + socket.socket().getInetAddress() + ":" + socket.socket().getPort());
		
	}
	
	/**
	 * Gets the protocol.
	 *
	 * @return the protocol
	 */
	public GameProtocol getProtocol() {
		return protocol;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			initialize();
		}
		catch (IOException e) {
			System.out.println("Error in initializing I/O");
		}
		while(!clientSocket.socket().isClosed() && !protocol.shouldClose()){
			try {
				process();
			} 
			catch (IOException e) {
				System.out.println("Error in I/O");
			} 
		}
		System.out.println("Connection closed - bye bye...");
		close();
	}
	
		/**
		 * Gets the message and send it to the protocol to process.
		 *
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public void process() throws IOException
	    {

	        while (true)
	        {
	            ByteBuffer buf = ByteBuffer.allocate(1024);
	            try{
	                clientSocket.read(buf);
	            }
	            catch (IOException e){
	                e.printStackTrace();
	            }

	            buf.flip();
	            tokenizer.addBytes(buf);

	            while (tokenizer.hasMessage()){

	                String str = tokenizer.nextMessage();
	                System.out.println("Received \"" + str + "\" from client");

	                protocol.processMessage(str,callback);

	                if (protocol.shouldClose()){
	                    close();
	                	break;
	                }
	            }
	            if (protocol.shouldClose())
	            	break;
	        }
	    }
		
		/**
		 * Adds the data to the output stream for sending to client.
		 *
		 * @param buff the buff
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
	public void addOutData(ByteBuffer buff) throws IOException {
		clientSocket.socket().getOutputStream().write(buff.array(), 0,buff.capacity());
	}

	/**
	 * Initialize the connection.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	// Starts listening
	public void initialize() throws IOException
	{
		// Initialize I/O
		in = new BufferedReader(new InputStreamReader(clientSocket.socket().getInputStream(),"UTF-8"));
		out = new PrintWriter(new OutputStreamWriter(clientSocket.socket().getOutputStream(),"UTF-8"), true);
		System.out.println("I/O initialized");
		
	}
	
	/**
	 * Close the connection.
	 */
	public void close()
	{
		try {
			if (in != null){
				in.close();
			}
			if (out != null){
				out.close();
			}
			protocol.connectionTerminated();
			clientSocket.close();
		} catch (IOException e){
			System.out.println("Exception in closing I/O");
		}
	}
	
	/**
	 * Gets the client socket.
	 *
	 * @return the client socket
	 */
	public SocketChannel getClientSocket() {
		return clientSocket;
	}
}

















/*import java.beans.Encoder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.Vector;


import messages.Message;

public class ConnectionHandler implements Runnable{
	private final Socket socket;
	private final Tokenizer tokenizer;
	private final GameProtocol protocol;
	private final Encoder encoder;
	private Vector<ByteBuffer> _out;
	private BufferedReader in;
	private PrintWriter out;
	
	ConnectionHandler(Socket socket, Tokenizer tokenizer,Encoder encoder, GameProtocol protocol){
		this.socket = socket;
		this.tokenizer = tokenizer;
		this.encoder = encoder;
		this.protocol = protocol;
	}
	
	
	public void initialize() throws IOException
	{
		// Initialize I/O
		in = new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));
		out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"), true);
		System.out.println("I/O initialized");
	}
	
	
	private boolean getBytes(char bytes[], int bytesToRead) {
	    int tmp = 0;
		//boost::system::error_code error;
	    try {
	        while (!error && bytesToRead > tmp ) {
				tmp += socket_.read_some(boost::asio::buffer(bytes+tmp, bytesToRead-tmp), error);			
	        }
			if(error)
				throw boost::system::system_error(error);
	    } catch (std::exception& e) {
	        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
	        return false;
	    }
	    return true;
	}
	public void run(){
		while(!protocol.shouldClose() && !socket.isClosed()){
			try{
				if(!tokenizer.isAlive())
					protocol.connectionTerminated();
				else {
				
					String msg = tokenizer.nextToken();
					while(msg)
					msg.sub
					ProtocolCallbackImp callback = new ProtocolCallbackImp(socket, encoder);
					protocol.processMessage(msg,callback);
				}
			} catch(IOException){
				protocol.connectionTerminated();
				break;
			}
			try{
				socket.close();
			}
		} 
	}

	public void addOutData(ByteBuffer buff) {
		// TODO Auto-generated method stub
		
	}
}

class Callback<T>{
	address
	
	callback(address)
	
	sendMessage(msg){
		//sends msg to address
	}
}


 
bool ConnectionHandler::getBytes(char bytes[], unsigned int bytesToRead) {
    size_t tmp = 0;
	boost::system::error_code error;
    try {
        while (!error && bytesToRead > tmp ) {
			tmp += socket_.read_some(boost::asio::buffer(bytes+tmp, bytesToRead-tmp), error);			
        }
		if(error)
			throw boost::system::system_error(error);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool ConnectionHandler::sendBytes(const char bytes[], int bytesToWrite) {
    int tmp = 0;
	boost::system::error_code error;
    try {
        while (!error && bytesToWrite > tmp ) {
			tmp += socket_.write_some(boost::asio::buffer(bytes + tmp, bytesToWrite - tmp), error);
        }
		if(error)
			throw boost::system::system_error(error);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}
 
bool ConnectionHandler::getLine(std::string& line) {
    return getFrameAscii(line, '\n');
}

bool ConnectionHandler::sendLine(std::string& line) {
    return sendFrameAscii(line, '\n');
}
 
bool ConnectionHandler::getFrameAscii(std::string& frame, char delimiter) {
    char ch;
    // Stop when we encounter the null character. 
    // Notice that the null character is not appended to the frame string.
    try {
		do{
			getBytes(&ch, 1);
            frame.append(1, ch);
        }while (delimiter != ch);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}
 
bool ConnectionHandler::sendFrameAscii(const std::string& frame, char delimiter) {
	bool result=sendBytes(frame.c_str(),frame.length());
	if(!result) return false;
	return sendBytes(&delimiter,1);
}
 
// Close down the connection properly.
void ConnectionHandler::close() {
    try{
        socket_.close();
    } catch (...) {
        std::cout << "closing failed: connection already closed" << std::endl;
    }
}
*/