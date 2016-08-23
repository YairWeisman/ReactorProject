import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.channels.SelectionKey;
import java.nio.channels.ClosedChannelException;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.Vector;
import java.util.logging.Logger;


/**
 * Handles messages from clients.
 *
 * @param <T> the generic type
 */
public class ConnectionHandlerReactor<T> {


	/** The Constant BUFFER_SIZE. */
	private static final int BUFFER_SIZE = 1024;

	/** The _s channel. */
	protected final SocketChannel _sChannel;

	/** The _data. */
	protected final ReactorData<T> _data;
	
	/** The callback. */
	private ProtocolCallback<String> callback;
	
	/** The _protocol. */
	protected final GameProtocol _protocol;
	
	/** The _tokenizer. */
	protected final FixedSeparatorMessageTokenizer _tokenizer;

	/** The _out data. */
	protected Vector<ByteBuffer> _outData = new Vector<ByteBuffer>();

	/** The _skey. */
	protected final SelectionKey _skey;

	/** The _task. */
	private ProtocolTask<T> _task = null;

	/**
	 * Creates a new ConnectionHandler object.
	 *
	 * @param sChannel            the SocketChannel of the client
	 * @param data            a reference to a ReactorData object
	 * @param key the key
	 */
	private ConnectionHandlerReactor(SocketChannel sChannel, ReactorData<T> data, SelectionKey key) {
		_sChannel = sChannel;
		_data = data;
		_protocol = _data.getProtocolMaker().create();
		final Charset charset = Charset.forName("UTF-8");
        final String separator = "\n";
		_tokenizer = _data.getTokenizerMaker().create(separator,charset);
		_skey = key;
		callback = new ProtocolCallback<String>() {

            public void sendMessage(String msg) throws IOException {
                if(msg != null){
                    ByteBuffer bytes = _tokenizer.getBytesForMessage(new String(msg.toString()));
                    _sChannel.write(bytes); // not sure CHECK AGAIN
                }
            }
        };
	}

	/**
	 * Initialize.
	 */
	private void initialize() {
		_skey.attach(this);
		_task = new ProtocolTask<T>(_protocol, _tokenizer, this);
	}

	/**
	 * Creates the connection handler.
	 *
	 * @param <T> the generic type
	 * @param sChannel the s channel
	 * @param data the data
	 * @param key the key
	 * @return the connection handler reactor
	 */
	public static <T> ConnectionHandlerReactor create(SocketChannel sChannel, ReactorData<T> data, SelectionKey key) {
		ConnectionHandlerReactor h = new ConnectionHandlerReactor(sChannel, data, key);
		h.initialize();
		return h;
	}

	/**
	 * Gets the callback.
	 *
	 * @return the callback
	 */
	public ProtocolCallback<String> getCallback() {
		return callback;
	}
	
	/**
	 * Adds the out data to the buffer.
	 *
	 * @param buf the buffer.
	 */
	public synchronized void addOutData(ByteBuffer buf) {
		_outData.add(buf);
		switchToReadWriteMode();
	}

	/**
	 * Close connection.
	 */
	private void closeConnection() {
		// remove from the selector.
		_skey.cancel();
		try {
			_sChannel.close();
		} catch (IOException ignored) {
			ignored = null;
		}
	}

	/**
	 * Reads incoming data from the client:
	 * <UL>
	 * <LI>Reads some bytes from the SocketChannel
	 * <LI>create a protocolTask, to process this data, possibly generating an
	 * answer
	 * <LI>Inserts the Task to the ThreadPool
	 * </UL>.
	 */
	public void read() {
		// do not read if protocol has terminated. only write of pending data is
		// allowed
		if (_protocol.shouldClose()) {
			return;
		}

		SocketAddress address = _sChannel.socket().getRemoteSocketAddress();
		System.out.println("Reading from " + address);

		ByteBuffer buf = ByteBuffer.allocate(BUFFER_SIZE);
		int numBytesRead = 0;
		try {
			numBytesRead = _sChannel.read(buf);
		} catch (IOException e) {
			numBytesRead = -1;
		}
		// is the channel closed?
		if (numBytesRead == -1) {
			// No more bytes can be read from the channel
			System.out.println("client on " + address + " has disconnected");
			closeConnection();
			// tell the protocol that the connection terminated.
			_protocol.connectionTerminated();
			return;
		}

		//add the buffer to the protocol task
		buf.flip();
		_task.addBytes(buf);
		// add the protocol task to the reactor
		_data.getExecutor().execute(_task);
	}

	/**
	 * attempts to send data to the client<br/>
	 * if all the data has been successfully sent, the ConnectionHandler will
	 * automatically switch to read only mode, otherwise it'll stay in it's
	 * current mode (which is read / write).
	 */
	public synchronized void write() {
		if (_outData.size() == 0) {
			// if nothing left in the output string, go back to read mode
			switchToReadOnlyMode();
			return;
		}
		// if there is something to send
		ByteBuffer buf = _outData.remove(0);
		if (buf.remaining() != 0) {
			try {
				_sChannel.write(buf);
			} catch (IOException e) {
				// this should never happen.
				e.printStackTrace();
			}
			// check if the buffer contains more data
			if (buf.remaining() != 0) {
				_outData.add(0, buf);
			}
		}
		// check if the protocol indicated close.
		if (_protocol.shouldClose()) {
			switchToWriteOnlyMode();
			if (buf.remaining() == 0) {
				closeConnection();
				SocketAddress address = _sChannel.socket().getRemoteSocketAddress();
				System.out.println("disconnecting client on " + address);
			}
		}
	}

	/**
	 * switches the handler to read / write.
	 */
	public void switchToReadWriteMode() {
		_skey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		_data.getSelector().wakeup();
	}

	/**
	 * switches the handler to read only mode.
	 */
	public void switchToReadOnlyMode() {
		_skey.interestOps(SelectionKey.OP_READ);
		_data.getSelector().wakeup();
	}

	/**
	 * switches the handler to write only mode.
	 */
	public void switchToWriteOnlyMode() {
		_skey.interestOps(SelectionKey.OP_WRITE);
		_data.getSelector().wakeup();
	}

}
