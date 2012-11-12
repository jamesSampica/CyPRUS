package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.net.ServerSocketFactory;

import database.JDBCDatabase;

public class Server {

	private volatile boolean connected = true;

	//Threaded
	private volatile List<ServerClient> clients = Collections.synchronizedList(new ArrayList<ServerClient>());
	private volatile ServerSocket server;

	private int port;
	private JDBCDatabase database;
	
	public Server(int port, String dbUserName, String dbPassword) {
		try {
			this.port = port;
			database = new JDBCDatabase(dbUserName, dbPassword);
			server = ServerSocketFactory.getDefault().createServerSocket(this.port);
		} catch (IOException e) {
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
				
				//If we are in a blocking state then close the server to get out of that state
				server.close();
				
				System.out.println("...Done");
			}
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void start() { //This method is threaded
		try {			
			//while the server is connected continue receiving clients
			while (server.isBound() && this.isConnected()) {
				Socket client = server.accept();
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
			source.writeMessage(database.queryDatabase(message));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
