package testing;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.junit.Test;

import server.Server;
import server.ServerClient;

public class ServerLoadTest {

	private static int testPort = 123;
	
	@Test
	public void testClientLoad() {
		
		ArrayList<ServerClient> clients = new ArrayList<ServerClient>();
		final Server testServer = new Server(testPort, "admin", "admin");
		
		Runnable r = new Runnable() {
			@Override
			public void run() {
				testServer.start();
			};
		};
		new Thread(r).start();
		
		try{
			for(int i = 0; i < 200; i++){
				System.out.println("Inserting Client #" + i);
				clients.add(new ServerClient(new Socket("localhost", testPort), testServer));
			}
			
			for(ServerClient sc: clients){
				byte[] b = {'t','e','s','t'};
				
				sc.onMessage(b);
			}
		}
		catch(Exception e){
			fail(e.getMessage());
		}
		
	}
}
