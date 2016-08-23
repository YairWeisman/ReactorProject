import java.io.IOException;
/**
 * The Interface Tokenizer.
 */
public interface Tokenizer {

	/**
	 * Checks if is alive.
	 *
	 * @return true, if is alive
	 */
	public boolean isAlive();
	
	/**
	 * Next token.
	 *
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public String nextToken() throws IOException;
}
