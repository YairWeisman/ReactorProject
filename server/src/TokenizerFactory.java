import java.nio.charset.Charset;

/**
 * A factory for creating Tokenizer objects.
 *
 * @param <T> the generic type
 */
public interface TokenizerFactory<T> {
   
   /**
    * Creates the.
    *
    * @param separator the separator
    * @param charset the charset
    * @return the fixed separator message tokenizer
    */
   FixedSeparatorMessageTokenizer create(String separator, Charset charset);
}
