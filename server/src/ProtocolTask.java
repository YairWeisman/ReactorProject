

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;


// TODO: Auto-generated Javadoc
/**
 * This class supplies some data to the protocol, which then processes the data,
 * possibly returning a reply. This class is implemented as an executor task.
 *
 * @param <T> the generic type
 */
public class ProtocolTask<T> implements Runnable {

	/** The _protocol. */
	private final GameProtocol _protocol;
	
	/** The _tokenizer. */
	private final FixedSeparatorMessageTokenizer _tokenizer;
	
	/** The _handler. */
	private final ConnectionHandlerReactor _handler;
	
	/** The callback. */
	private ProtocolCallback<T> callback;
	
	/**
	 * Instantiates a new protocol task.
	 *
	 * @param protocol the protocol
	 * @param tokenizer the tokenizer
	 * @param h the h
	 */
	public ProtocolTask(final GameProtocol protocol, final FixedSeparatorMessageTokenizer tokenizer, final ConnectionHandlerReactor h) {
		this._protocol = protocol;
		this._tokenizer = tokenizer;
		this._handler = h;
		callback=call->{
			if (call != null) {
				try {
					ByteBuffer bytes = _tokenizer.getBytesForMessage(call);
					this._handler.addOutData(bytes);
				} catch (CharacterCodingException e) { e.printStackTrace(); }
			}
		};
	}

	// we synchronize on ourselves, in case we are executed by several threads
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	// from the thread pool.
	public synchronized void run() {
		// go over all complete messages and process them.
		while (_tokenizer.hasMessage()) {
			String msg = _tokenizer.nextMessage();

			try {
				this._protocol.processMessage(msg, (ProtocolCallback<String>) callback);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Adds the bytes.
	 *
	 * @param b the byte buffer
	 */
	public void addBytes(ByteBuffer b) {
		_tokenizer.addBytes(b);
	}
}
