package support;

import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * An abstract class that defines some of the methods and implementations for server clients and regular clients
 * @author James
 *
 */
public abstract class BaseMessageClient implements MessageClient {

	private Socket socket;
	private MessageReader reader;
	private MessageWriter writer;

	/**
	 * Creates a new message client given a host and port
	 * @param host the host name or ip
	 * @param port the port number
	 * @throws Exception if any error occurs
	 */
	public BaseMessageClient(String host, int port) throws Exception {	
		this(new Socket(host, port));
	}

	/**
	 * Creates a new message client given a socket
	 * @param socket the client socket
	 */
	public BaseMessageClient(Socket socket) {
		try {
			this.socket = socket;
			//System.out.println("Creating reader");
			//System.out.printf("Socket - %s%n\n", socket);
			this.reader = new MessageReader(this, this.socket.getInputStream());
			//System.out.println("Creating writer");
			this.writer = new MessageWriter(this, this.socket.getOutputStream());
			new Thread(this.reader).start();
			new Thread(this.writer).start();
			//System.out.println("Started threads");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * Gets the reader associated with this client
	 * @return the client's reader
	 */
	public MessageReader getReader() {
		return reader;
	}
	
	/**
	 * Gets the writer associated with this client
	 * @return the client's writer
	 */
	public MessageWriter getWriter() {
		return writer;
	}
	
	/**
	 * Gets the socket associated with this client
	 * @return the client's socket
	 */
	public Socket getSocket() {
		return socket;
	}
	
	@Override
	public void errorOnWrite( Exception e ) {
		//System.out.println( "An error happened while writing: " + e.getMessage() );
		Logger.getLogger(BaseMessageClient.class.getName()).log(Level.WARNING,  "An error happened while writing: " + e.getMessage() );
	}
	
	/**
	 * Writes a message to the currently connected socket
	 * @param message the message in bytes to be written
	 */
	public void writeMessage( byte[] message ) {
		this.getWriter().writeMessage(message);
	}

	@Override
	public void errorOnRead(Exception e) {
		//System.out.println( "An error happened while reading: " + e.getMessage());	
		Logger.getLogger(BaseMessageClient.class.getName()).log(Level.WARNING,  "An error happened while reading: " + e.getMessage());
	}	
	
	/**
	 * Disconnects this client permanently, shutting off all reads and writes from the socket
	 */
	public void disconnect() {
		this.reader.setConnected(false);
		this.writer.setConnected(false);
		try {
			Thread.sleep(1000);
			this.socket.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
