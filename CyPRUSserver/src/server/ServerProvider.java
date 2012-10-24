package server;
import java.io.*;
import java.net.*;

public class ServerProvider{
	private ServerSocket providerSocket;
	private Socket connection = null;
	private ObjectOutputStream socketOut;
	private ObjectInputStream socketIn;
	private int port;
	private JDBCDatabase database;
	
	public ServerProvider(int port){
		this.port = port;
		database = new JDBCDatabase("jim", "hickory");
	}
	public void run(){
		try{
			//1. creating a server socket
			providerSocket = new ServerSocket(this.port, 10);
			
			//2. Wait for connection
			ServerFrontEnd.info("Waiting for connection");
			connection = providerSocket.accept();
			ServerFrontEnd.info("Connection received from " + connection.getInetAddress().getHostName());
			//3. get Input and Output streams
			socketOut = new ObjectOutputStream(connection.getOutputStream());
			socketOut.flush();
			socketIn = new ObjectInputStream(connection.getInputStream());
			sendMessage("Connection successful");
			//4. The two parts communicate via the input and output streams
			String message = "";
			while(message != null && !message.equals("kill")){
				try{
					message = (String)socketIn.readObject();
					System.out.println("client> " + message);
					String result = database.queryDatabase(message);
					sendMessage(result);
				}
				catch(ClassNotFoundException classnot){
					System.err.println("Data received in unknown format");
				}
			}
		}
		catch(IOException ioException){
			ServerFrontEnd.info("server failed, stopping...");
			System.exit(0);
		}
		finally{
			//4: Closing connection
			try{
				socketIn.close();
				socketOut.close();
				providerSocket.close();
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
	}
	public void sendMessage(String msg)
	{
		try{
			socketOut.writeObject(msg);
			socketOut.flush();
			ServerFrontEnd.info("server> " + msg);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	public int requestType(String message){
		if(message.contains("select")){
			return 0;
		}
		else if(message.contains("insert")){
			return 1;
		}
		else if(message.contains("update")){
			return 2;
		}
		else if(message.contains("delete")){
			return 3;
		}
		return -1;
	}
}