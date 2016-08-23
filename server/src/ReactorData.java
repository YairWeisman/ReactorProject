

import java.util.concurrent.ExecutorService;
import java.nio.channels.Selector;
/**
 * a simple data structure that hold information about the reactor, including getter methods.
 *
 * @param <T> the generic type
 */
public class ReactorData<T> {

    /** The _executor. */
    private final ExecutorService _executor;
    
    /** The _selector. */
    private final Selector _selector;
    
    /** The _protocol maker. */
    private final GameProtocolFactory _protocolMaker;
    
    /** The _tokenizer maker. */
    private final TokenizerFactory<T> _tokenizerMaker;
    
    /**
     * Gets the executor.
     *
     * @return the executor
     */
    public ExecutorService getExecutor() {
        return _executor;
    }

    /**
     * Gets the selector.
     *
     * @return the selector
     */
    public Selector getSelector() {
        return _selector;
    }

	/**
	 * Instantiates a new reactor data.
	 *
	 * @param _executor the _executor
	 * @param _selector the _selector
	 * @param protocol the protocol
	 * @param tokenizer the tokenizer
	 */
	public ReactorData(ExecutorService _executor, Selector _selector, GameProtocolFactory protocol, TokenizerFactory<T> tokenizer) {
		this._executor = _executor;
		this._selector = _selector;
		this._protocolMaker = protocol;
		this._tokenizerMaker = tokenizer;
	}

	/**
	 * Gets the protocol maker.
	 *
	 * @return the protocol maker
	 */
	public GameProtocolFactory getProtocolMaker() {
		return _protocolMaker;
	}

	/**
	 * Gets the tokenizer maker.
	 *
	 * @return the tokenizer maker
	 */
	public TokenizerFactory<T> getTokenizerMaker() {
		return _tokenizerMaker;
	}

}
