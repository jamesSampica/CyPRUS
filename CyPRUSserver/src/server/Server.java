package server;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import database.JDBCDatabase;

public class Server {

	private volatile boolean connected = true;

	//Threaded
	private volatile List<ServerClient> clients = Collections.synchronizedList(new ArrayList<ServerClient>());
	private volatile SSLServerSocket server;

	private int port;
	
	public Server(int port, String dbUserName, String dbPassword) {
		try {
			this.port = port;
			JDBCDatabase.setupJDBCDatabase(dbUserName,dbPassword);
			
		        // KeyStore ks = KeyStore.getInstance("JKS");
		        // ks.load(new FileInputStream("iastatecyprus.jks"), "iastate".toCharArray());
		        // KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		        // kmf.init(ks, "iastate".toCharArray());
		         //SSLContext sc = SSLContext.getInstance("SSL");
		         //sc.init(kmf.getKeyManagers(), null, null);
		         SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
				server = (SSLServerSocket) ssf.createServerSocket(this.port);
		} catch (IOException e) {
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		try{
			this.connected = connected;
			if (!this.connected) {
				System.out.println("Cleaning up...");
				
				//Disconnect all clients connected to the server
				for (ServerClient client : this.clients) {
					client.disconnect();
				}
				
				//If in a blocking state then close the server to get out of that state
				server.close();
				
				System.out.println("...Done");
				System.out.println("Goodbye...");
			}
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void start() { //This method is threaded
		try {			
			//while the server is connected continue receiving clients
			while (server.isBound() && this.isConnected()) {
				SSLSocket client = (SSLSocket) server.accept();
				System.out.printf("Client connected... %s%n", client.getInetAddress());
				ServerClient serverClient = new ServerClient(client, this);
				this.clients.add(serverClient);


			}
		}
		catch(SocketException se){
			//Do nothing
			//This is needed so that when we exit the program, the server.accept() doesn't stay in a blocking state
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onMessage(String message, ServerClient source) {
		//Query db
		try {
			source.writeMessage(JDBCDatabase.getDatabase().queryDatabase(message));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
