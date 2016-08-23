

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


// TODO: Auto-generated Javadoc
/**
 * An implementation of the Reactor pattern.
 *
 * @param <T> the generic type
 */
public class Reactor<T> implements Runnable {

    /** The _port. */
    private final int _port;

    /** The _pool size. */
    private final int _poolSize;

    /** The _protocol factory. */
    private final GameProtocolFactory _protocolFactory;

    /** The _tokenizer factory. */
    private final TokenizerFactory<T> _tokenizerFactory;

    /** The _should run. */
    private volatile boolean _shouldRun = true;

    /** The _data. */
    private ReactorData<T> _data;

    /**
     * Creates a new Reactor.
     *
     * @param port      the port to bind the Reactor to
     * @param poolSize  the number of WorkerThreads to include in the ThreadPool
     * @param protocol  the protocol factory to work with
     * @param tokenizer the tokenizer factory to work with
     */
    public Reactor(int port, int poolSize, GameProtocolFactory protocol, TokenizerFactory<T> tokenizer) {
        _port = port;
        _poolSize = poolSize;
        _protocolFactory = protocol;
        _tokenizerFactory = tokenizer;
    }

    /**
     * Create a non-blocking server socket channel and bind to to the Reactor
     * port.
     *
     * @param port the port
     * @return the server socket channel
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private ServerSocketChannel createServerSocket(int port)
            throws IOException {
        try {
            ServerSocketChannel ssChannel = ServerSocketChannel.open();
            ssChannel.configureBlocking(false);
            ssChannel.socket().bind(new InetSocketAddress(port));
            return ssChannel;
        } catch (IOException e) {
            System.out.println("Port " + port + " is busy");
            throw e;
        }
    }

    /**
     * Main operation of the Reactor:
     * <UL>
     * <LI>Uses the <CODE>Selector.select()</CODE> method to find new
     * requests from clients
     * <LI>For each request in the selection set:
     * <UL>
     * If it is <B>acceptable</B>, use the ConnectionAcceptor to accept it,
     * create a new ConnectionHandler for it register it to the Selector
     * <LI>If it is <B>readable</B>, use the ConnectionHandler to read it,
     * extract messages and insert them to the ThreadPool
     * </UL>
     */
    public void run() {
        // Create & start the ThreadPool
        ExecutorService executor = Executors.newFixedThreadPool(_poolSize);
        Selector selector = null;
        ServerSocketChannel ssChannel = null;

        try {
            selector = Selector.open();
            ssChannel = createServerSocket(_port);
        } catch (IOException e) {
            System.out.println("cannot create the selector -- server socket is busy?");
            return;
        }

        _data = new ReactorData<T>(executor, selector, _protocolFactory, _tokenizerFactory);
        ConnectionAcceptor<T> connectionAcceptor = new ConnectionAcceptor<T>(ssChannel, _data);

        // Bind the server socket channel to the selector, with the new
        // acceptor as attachment

        try {
            ssChannel.register(selector, SelectionKey.OP_ACCEPT, connectionAcceptor);
        } catch (ClosedChannelException e) {
        	System.out.println("server channel seems to be closed!");
            return;
        }

        while (_shouldRun && selector.isOpen()) {
            // Wait for an event
            try {
                selector.select();
            } catch (IOException e) {
                System.out.println("trouble with selector: " + e.getMessage());
                continue;
            }

            // Get list of selection keys with pending events
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();

            // Process each key
            while (it.hasNext()) {
                // Get the selection key
                SelectionKey selKey = (SelectionKey) it.next();

                // Remove it from the list to indicate that it is being
                // processed. it.remove removes the last item returned by next.
                it.remove();

                // Check if it's a connection request
                if (selKey.isValid() && selKey.isAcceptable()) {
                	System.out.println("Accepting a connection");
                    ConnectionAcceptor<T> acceptor = (ConnectionAcceptor<T>) selKey.attachment();
                    try {
                        acceptor.accept();
                    } catch (IOException e) {
                    	System.out.println("problem accepting a new connection: "
                                + e.getMessage());
                    }
                    continue;
                }
                // Check if a message has been sent
                if (selKey.isValid() && selKey.isReadable()) {
                    ConnectionHandlerReactor handler = (ConnectionHandlerReactor) selKey.attachment();
                    System.out.println("Channel is ready for reading");
                    handler.read();
                }
                // Check if there are messages to send
                if (selKey.isValid() && selKey.isWritable()) {
                    ConnectionHandlerReactor handler = (ConnectionHandlerReactor) selKey.attachment();
                    System.out.println("Channel is ready for writing");
                    handler.write();
                }
            }
        }
        stopReactor();
    }

    /**
     * Returns the listening port of the Reactor.
     *
     * @return the listening port of the Reactor
     */
    public int getPort() {
        return _port;
    }

    /**
     * Stops the Reactor activity, including the Reactor thread and the Worker
     * Threads in the Thread Pool.
     */
    public synchronized void stopReactor() {
        if (!_shouldRun)
            return;
        _shouldRun = false;
        _data.getSelector().wakeup(); // Force select() to return
        _data.getExecutor().shutdown();
        try {
            _data.getExecutor().awaitTermination(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // Someone didn't have patience to wait for the executor pool to
            // close
            e.printStackTrace();
        }
    }

    /**
     * Main program, used for demonstration purposes. Create and run a
     * Reactor-based server for the Echo protocol. Listening port number and
     * number of threads in the thread pool are read from the command line.
     *
     * @param args the arguments
     */
    public static void main(String args[]) {
       /* if (args.length != 2) {
            System.err.println("Usage: java Reactor <port> <pool_size>");
            System.exit(1);
        }*/

        try {
            //int port = Integer.parseInt(args[0]);
        	int port =2456;
            //int poolSize = Integer.parseInt(args[1]);
        	int poolSize = 2;
            Reactor<String> reactor = startGameServer(port, poolSize);

            Thread thread = new Thread(reactor);
            thread.start();
            System.out.println("Reactor is ready on port " + reactor.getPort());
            GameBoard gameBoard = GameBoard.getInstance();
            gameBoard.addGame(new Bluffer());
            thread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Start game server.
     *
     * @param port the port
     * @param poolSize the pool size
     * @return the reactor
     */
    public static Reactor<String> startGameServer(int port, int poolSize) {
        GameProtocolFactory protocolMaker = new GameProtocolFactory();
        final Charset charset = Charset.forName("UTF-8");
        final String separator = "\n";
        TokenizerFactory<String> tokenizerMaker = new TokenizerFactory<String>() {
            public FixedSeparatorMessageTokenizer create(String separator, Charset charset) {
                return new FixedSeparatorMessageTokenizer(separator, charset);
            }
        };

        Reactor<String> reactor = new Reactor<String>(port, poolSize, protocolMaker, tokenizerMaker);
        return reactor;
    }
}
