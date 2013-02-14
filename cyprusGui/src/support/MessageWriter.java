package support;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MessageWriter implements Runnable {

	private MessageClient client;
	private DataOutputStream stream;
	private BlockingQueue<byte[]> queue = new LinkedBlockingQueue<byte[]>();
	private volatile boolean connected = true;
	
	public MessageWriter( MessageClient client, OutputStream stream ) {
		this.client = client;
		this.stream = new DataOutputStream( stream );
		System.out.println("Writer is ready");
	}

	public boolean isConnected() {
		return connected;
	}
	
	public void setConnected(boolean connected) {
		this.connected = connected;
	}
	
	public void writeMessage( byte[] message ) {
		this.queue.offer(message);
	}
	
	@Override
	public void run() {

		while ( this.isConnected() ) {
			try {/*
				String message = this.queue.poll( 1 , TimeUnit.SECONDS);
				if ( message != null ) {
					byte[] bytes = message.getBytes();
					this.stream.writeInt( bytes.length );
					this.stream.write( bytes );
				}*/
				byte[] message = this.queue.poll( 1 , TimeUnit.SECONDS);
				if ( message != null ) {
					this.stream.writeInt( message.length );
					this.stream.write( message );
				}
			} catch (InterruptedException e) {
				System.out.println("Byte Stream was interrupted " + e.getMessage());
			} catch ( IOException e ) {
				this.client.errorOnWrite(e);
			}
		}
		
	}


}
