package server;

import java.net.Socket;

import support.BaseMessageClient;

public class ServerClient extends BaseMessageClient {

	private Server server;
	
	public ServerClient(Socket client, Server server) {
		super(client);
		this.server = server;
	}

	@Override
	public void onMessage(String message) {
		System.out.println( message );
		this.server.onMessage(message, this);
	}

}
