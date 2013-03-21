package support;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Defines a common class for network writes 
 * @author James Sampica
 *
 */
public class MessageWriter implements Runnable {

	private MessageClient client;
	private DataOutputStream stream;
	private BlockingQueue<byte[]> queue = new LinkedBlockingQueue<byte[]>();
	private volatile boolean connected = true;
	
	/**
	 * Creates a new writer object to connect the client and writer stream
	 * @param client the client to connect the writer to
	 * @param stream the stream to connect the client to
	 */
	public MessageWriter( MessageClient client, OutputStream stream ) {
		this.client = client;
		this.stream = new DataOutputStream( stream );
		//System.out.println("Writer is ready");
	}

	/**
	 * Tests whether this writer is in an active state
	 * @return true if active, false otherwise
	 */
	public boolean isConnected() {
		return connected;
	}
	
	/**
	 * Sets the connection state of this writer
	 * @param connected the new connection state of the writer
	 */
	public void setConnected(boolean connected) {
		this.connected = connected;
	}
	
	/**
	 * writes a message to the output stream
	 * @param message the message in bytes to be written
	 */
	public void writeMessage( byte[] message ) {
		this.queue.offer(message);
	}
	
	@Override
	public void run() {

		while ( this.isConnected() ) {
			try {
				byte[] message = this.queue.poll( 1 , TimeUnit.SECONDS);
				if ( message != null ) {
					this.stream.writeInt( message.length );
					this.stream.write( message );
				}
			} catch (InterruptedException e) {
				//System.out.println("Byte Stream was interrupted " + e.getMessage());
			} catch ( IOException e ) {
				this.setConnected(false);
				this.client.errorOnWrite(e);
			}
		}
		
	}


}
