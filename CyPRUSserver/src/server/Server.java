package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ServerSocketFactory;

import support.Packet;
import support.Vehicle;
import utils.SerializationUtils;
import database.JDBCDatabase;

/**
 * This class is the actual server that handles client connections and vehicle
 * @author James
 *
 */
public class Server {

	private volatile boolean connected = true;

	//Threaded
	private volatile List<ServerClient> clients;
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(64);
	private volatile List<Vehicle> activeVehicles;
	private volatile List<Vehicle> recentVehicles;
	private volatile ServerSocket server;

	private int port;
	
	/**
	 * Sets up a new server to begin tracking vehicles and clients
	 * @param port the port to listen on
	 * @param dbUserName the database username
	 * @param dbPassword the database password
	 */
	public Server(int port, String dbUserName, String dbPassword) {
		try {
			this.port = port;
			
			clients = Collections.synchronizedList(new ArrayList<ServerClient>());
			activeVehicles = Collections.synchronizedList(new ArrayList<Vehicle>());
			recentVehicles = Collections.synchronizedList(new ArrayList<Vehicle>());
			
			JDBCDatabase.setupJDBCDatabase(dbUserName,dbPassword);

		    ServerSocketFactory ssf = (ServerSocketFactory) ServerSocketFactory.getDefault();
		    server = (ServerSocket) ssf.createServerSocket(this.port);
		} catch (IOException e) {
			//System.out.println("An IO error occurred creating socket factory" + e.getMessage());
			Logger.getLogger(Server.class.getName()).log(Level.SEVERE, "An IO error occurred creating socket factory" + e.getMessage());
		} catch(Exception e){
			//System.out.println("An error occurred creating socket factory" + e.getMessage());
			Logger.getLogger(Server.class.getName()).log(Level.SEVERE, "An error occurred creating socket factory" + e.getMessage());
		}
	}

	/**
	 * tests whether the server is in a connected state
	 * @return true if connected, false otherwise
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * Sets the server connected state
	 * @param connected the connection state to set. Cleans up all clients if false
	 */
	public void setConnected(boolean connected) {
		try{
			this.connected = connected;
			if (!this.connected) {
				System.out.println("Cleaning up...");

				//Disconnect all clients connected to the server
				for (ServerClient client : this.clients) {
					client.disconnect();
				}
				
				scheduler.shutdownNow();
				
				//If in a blocking state then close the server to get out of that state
				server.close();
				
				System.out.println("...Done");
				System.out.println("Goodbye...");
			}
		
		} catch (IOException e) {
			//System.out.println("An error occurred when disconnecting" + e.getMessage());
			Logger.getLogger(Server.class.getName()).log(Level.SEVERE, "An error occurred when disconnecting" + e.getMessage());
		}
	}

	/**
	 * Starts the server, opens the server connection and lets clients connect
	 */
	public void start() { 
		try {			
			//while the server is connected continue receiving clients
			while (server.isBound() && this.isConnected()) {
				Socket client = (Socket) server.accept();
				//System.out.printf("Client connected... %s%n", client.getInetAddress());
				ServerClient serverClient = new ServerClient(client, this);
				this.clients.add(serverClient);
			}
		}
		catch(SocketException se){
			//Do nothing
			//This is needed so that when we exit the program, the server.accept() doesn't stay in a blocking state
		}
		catch (Exception e) {
			//System.out.println("An error occurred during client connection " + e.getMessage());
			Logger.getLogger(Server.class.getName()).log(Level.WARNING, "An error occurred during client connection " + e.getMessage());
		}
	}

	/**
	 * Processes a vehicle for tracking through the server
	 * @param capturedVehicle the captured vehicle to process
	 */
	public void processReceivedData(final Vehicle capturedVehicle) {
		
		//If the vehicle is entering for the first time
		if(!activeVehicles.contains(capturedVehicle)){

			Date platePass = JDBCDatabase.queryVehicleDatePass(capturedVehicle);
			int timePass = JDBCDatabase.queryVehicleTimePass(capturedVehicle);
			
			if(platePass == null || platePass.getTime() - System.currentTimeMillis() <= 0){
				activeVehicles.add(capturedVehicle);
				
				//Schedule a grace period for the vehicle
				scheduler.schedule(new Runnable() {
					public void run() { 
						
						//If the vehicle hasn't been removed from active vehicles
						if(activeVehicles.contains(capturedVehicle)){
							//System.out.println("Violation detected");
							
							//Store that plate/lot combo in db
							JDBCDatabase.storeParkingViolation(capturedVehicle);
							
							activeVehicles.remove(capturedVehicle);
							recentVehicles.add(recentVehicles.size(), capturedVehicle);
						}
					}
				}, Vehicle.GraceQuantumMillis + timePass, TimeUnit.MILLISECONDS);
			}
			else{
				//Valid date pass, do nothing
				//System.out.println("Do nothing");
				return;
			}
			
		}
		else{
			activeVehicles.remove(capturedVehicle);
		}
		
		//serialize vehicle to send
		Packet toSend = new Packet(Packet.CapturedVehicleCommand);
		toSend.setData(capturedVehicle);
		byte[] packetBytes = SerializationUtils.packetToBytes(toSend);
		
		//send data to all clients
		for(ServerClient sc: clients){
			if(sc.getWriter().isConnected() && sc.getReader().isConnected()){
				sc.writeMessage(packetBytes);
			}
		}
	}

	/**
	 * Processes a client's request to send it the current status of vehicle information
	 * @param requester the client requesting data
	 */
	public void processActiveVehiclesRequest(ServerClient requester){
		synchronized(activeVehicles){
			for(Vehicle activeVehicle: activeVehicles){
				//serialize vehicle to send
				Packet toSend = new Packet(Packet.ActiveVehiclesCommand);
				toSend.setData(activeVehicle);
				byte[] packetBytes = SerializationUtils.packetToBytes(toSend);
				
				requester.writeMessage(packetBytes);
			}
		}
	}
	
	/**
	 * Processes a client's request to send it the recent vehicle violations
	 * @param requester the client requesting data
	 */
	public void processRecentVehiclesRequest(ServerClient requester){
		synchronized(recentVehicles){
			for(Vehicle recentVehicle: recentVehicles){
				//serialize vehicle to send
				Packet toSend = new Packet(Packet.RecentVehiclesCommand);
				toSend.setData(recentVehicle);
				byte[] packetBytes = SerializationUtils.packetToBytes(toSend);
				
				requester.writeMessage(packetBytes);
			}
		}
	}
}
