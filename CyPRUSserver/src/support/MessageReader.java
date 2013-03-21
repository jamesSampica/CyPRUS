package support;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Defines a common class for network reads
 * @author James Sampica
 *
 */
public class MessageReader implements Runnable {

	private static final Executor POOL = Executors.newFixedThreadPool(24);

	final private MessageClient client;
	private DataInputStream stream;
	private boolean connected = true;

	/**
	 * Creates a new reader object to connect a client and input stream
	 * @param client the client to connect a reader to
	 * @param stream the stream to connect to the client
	 */
	public MessageReader(MessageClient client, InputStream stream) {
		this.client = client;
		this.stream = new DataInputStream(stream);
		//System.out.println("Reader is ready");
	}

	/**
	 * Tests whether this reader is in a connected state
	 * @return
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * Sets the state of this reader
	 * @param connected the new state of the reader
	 */
	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	@Override
	public void run() {

		try {
			while (this.isConnected()) {
				int size = this.stream.readInt();
				//System.out.printf("Reading %d bytes%n", size);
				byte[] byteMessage = new byte[size];
				for (int x = 0; x < size; x++) {
					//System.out.printf("reading byte %d of %d%n", x+1, size);
					byteMessage[x] = (byte) this.stream.read();
				}
				final byte[] trueMessage = byteMessage;

				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						client.onMessage(trueMessage);
					}
				};
				POOL.execute(runnable);
			}
		}
		catch (EOFException eof){
			//When client disconnects don't error out
		}
		catch (Exception e) {
			this.client.errorOnRead(e);
		}

	}

}
