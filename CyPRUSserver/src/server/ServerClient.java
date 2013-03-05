package server;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import database.JDBCDatabase;

import support.BaseMessageClient;
import support.Packet;
import support.Vehicle;
import utils.SerializationUtils;
import utils.VehicleImageUtils;

public class ServerClient extends BaseMessageClient {

	private Server server;
	
	public ServerClient(Socket client, Server server) {
		super(client);
		this.server = server;
	}
	
	@Override
	public void onMessage(byte[] message) {
		
		Packet packet = SerializationUtils.bytesToPacket(message);
		
		if(packet == null){
			processImageReceive(message);
		}
		else if(packet.getCommand() == Packet.ActiveVehiclesCommand){
			processActiveVehiclesRequest();
		}
		else if(packet.getCommand() == Packet.SearchCommand){
			processSearchRequest(packet);
		}
		else if(packet.getCommand() == Packet.RecentVehiclesCommand){
			this.server.processRecentVehiclesRequest(this);
		}
		
	}

	private void processActiveVehiclesRequest(){
		this.server.processActiveVehiclesRequest(this);
	}
	
	private void processSearchRequest(Packet message){
		
		if(!message.getSearchString().isEmpty()){
			ArrayList<Vehicle> queryResult = JDBCDatabase.getDatabase().queryVehicleViolations(message.getSearchString());
			for(Vehicle v: queryResult){
				Packet toSend = new Packet(Packet.SearchCommand);
				toSend.setVehicle(v);
				byte[] packetBytes = SerializationUtils.packetToBytes(toSend);
				writeMessage(packetBytes);
			}
		}
	}
	private void processImageReceive(byte[] message){			
			try {
				//test code
				Path dir = Paths.get("./plateStorage/spoonz.jpg"); 
				
				//test code
				byte[] b = Files.readAllBytes(dir);
				
				//String[] nodeDataSplit = VehicleImageUtils.getPlateAndLotFromBytes(message);
				
				//test code
		    	String[] nodeDataSplit = VehicleImageUtils.getPlateAndLotFromBytes(b);
		    	
		    	if(nodeDataSplit != null && nodeDataSplit.length >= 2){
		    		Vehicle capturedVehicle = new Vehicle(nodeDataSplit[0], nodeDataSplit[1]);
		    		
		    		//test code
		    		capturedVehicle.setImageBytes(b);
		    		
	                //capturedVehicle.setImageBytes(message);
	                
	                this.server.processReceivedData(capturedVehicle);
		    	}
    			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
}
