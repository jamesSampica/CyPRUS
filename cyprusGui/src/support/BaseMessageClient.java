package support;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public abstract class BaseMessageClient implements MessageClient {

	private SSLSocket socket;
	private MessageReader reader;
	private MessageWriter writer;

	public BaseMessageClient(String host, int port) throws Exception {	
		this(prepareSSLSocket(host, port));
	}

	public BaseMessageClient(SSLSocket socket) {
		try {
			this.socket = socket;
			System.out.println("Creating reader");
			System.out.printf("Socket - %s%n", socket);
			System.out.printf("Input - %s%n", this.socket.getInputStream());
			System.out.printf("Output - %s%n", this.socket.getOutputStream());
			this.reader = new MessageReader(this, this.socket.getInputStream());
			System.out.println("Creating writer");
			this.writer = new MessageWriter(this, this.socket.getOutputStream());
			new Thread(this.reader).start();
			new Thread(this.writer).start();
			System.out.println("Started threads");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	public MessageReader getReader() {
		return reader;
	}
	
	public MessageWriter getWriter() {
		return writer;
	}
	
	public SSLSocket getSocket() {
		return socket;
	}
	
	@Override
	public void errorOnWrite( Exception e ) {
		System.out.println( "An error happened while writing" );
		e.printStackTrace();
	}
	
	public void writeMessage( String message ) {
		this.getWriter().writeMessage(message);
	}

	@Override
	public void errorOnRead(Exception e) {
		System.out.println( "An error happened while reading" );
		e.printStackTrace();		
	}	
	
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
	private static SSLSocket prepareSSLSocket(String host, int port){	
		try {
			SSLSocketFactory f = (SSLSocketFactory) SSLSocketFactory.getDefault();
			return (SSLSocket) f.createSocket(host, port);
		} catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}
}
