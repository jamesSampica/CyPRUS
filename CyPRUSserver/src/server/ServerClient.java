package server;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import database.JDBCDatabase;

import support.BaseMessageClient;
import support.Packet;
import support.Vehicle;
import utils.SerializationUtils;
import utils.VehicleImageUtils;

/**
 * This class provides the server with a worker to which requests are handled internally or 
 * delegated to the server to handle. Each client connection sets up one of these as a connection pipeline
 * @author James Sampica
 *
 */
public class ServerClient extends BaseMessageClient {

	private Server server;
	
	/**
	 * Sets up a new message client that handles requests to the server and delegates as necessary
	 * @param client the connected client's Socket
	 * @param server the owner of this client
	 */
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
		else if(packet.getCommand() == Packet.InsertDatePassCommand){
			processInsertDatePass(packet);
		}
		else if(packet.getCommand() == Packet.InsertTimePassCommand){
			processInsertTimePass(packet);
		}
		
	}
	
	private void processInsertTimePass(Packet message){
		Vehicle v = (Vehicle) message.getData();
		if(v != null && v.getTimePassAmount() > 0){
			JDBCDatabase.insertVehicleTimePass(v, v.getTimePassAmount());
		}
	}
	
	private void processInsertDatePass(Packet message){
		Vehicle v = (Vehicle) message.getData();
		if(v != null && v.getGraceEndDate() != null){
			JDBCDatabase.insertVehicleDatePass(v, v.getGraceEndDate());
		}
	}
	
	private void processActiveVehiclesRequest(){
		this.server.processActiveVehiclesRequest(this);
	}
	
	private void processSearchRequest(Packet message){
		String searchString = (String) message.getData();
		if(searchString != null && !searchString.isEmpty()){
			ArrayList<Vehicle> queryResult = JDBCDatabase.queryVehicleViolations(searchString);
			for(Vehicle v: queryResult){
				Packet toSend = new Packet(Packet.SearchCommand);
				toSend.setData(v);
				byte[] packetBytes = SerializationUtils.packetToBytes(toSend);
				writeMessage(packetBytes);
			}
		}
	}
	private void processImageReceive(byte[] message){			
			//try {
				//test code
				//Path dir = Paths.get("./plateStorage/violationplates/spoonz.jpg"); 
				
				//test code
				//byte[] b = Files.readAllBytes(dir);
				
				try{
					String fileName = String.valueOf(System.currentTimeMillis());
					
					//Save image to temp directory
					//deploy code
					saveRawImage(message, fileName);
					
					//test code
					//saveTempImage(b, fileName);
					
					//Execute c image processing
					processCharacterRecognition(fileName);
					
					//Read image with metadata back in
					message = getImageWithMetadata(fileName);
				}
				catch(Exception e){
					Logger.getLogger(BaseMessageClient.class.getName()).log(
							Level.SEVERE,
							"processImageReceive Failed to do character recognition: "
									+ e.getMessage());
				}
				
				String[] nodeDataSplit = VehicleImageUtils.getPlateAndLotFromBytes(message);
		    	
		    	if(nodeDataSplit != null && nodeDataSplit.length >= 2){
		    		Vehicle capturedVehicle = new Vehicle(nodeDataSplit[0], nodeDataSplit[1]);

	                capturedVehicle.setImageBytes(message);
	                
	                this.server.processReceivedData(capturedVehicle);
		    	}
    			
			//} catch (IOException e) {
				//Logger.getLogger(BaseMessageClient.class.getName()).log(Level.WARNING,  " processImageReceive IOException: " + e.getMessage() );
			//}
	}
	
	private void saveRawImage(byte[] message, String fileName) throws Exception{
    		String directoryPath = "./plateStorage/temp/";
    		
    		InputStream in = new ByteArrayInputStream(message);
    		
    	    BufferedImage bi = ImageIO.read(in);
    	    File outputfile = new File(directoryPath + fileName + ".jpg");
    	    ImageIO.write(bi, "jpg", outputfile);
	}
	
	private void processCharacterRecognition(String fileName) throws Exception{
			String directoryPath = "./plateStorage/temp/" + fileName + ".jpg";
			Runtime r = Runtime.getRuntime();
			Process p = r.exec("../localize " + directoryPath);
			p.waitFor();
	}
	
	private byte[] getImageWithMetadata(String filename) throws Exception{
			String directoryPath = "./plateStorage/temp/" + filename + ".jpg";
			Path imagePath = Paths.get(directoryPath);
			byte[] imageBytes = Files.readAllBytes(imagePath);
			
			//Remove the file
			Runtime r = Runtime.getRuntime();
			Process p = r.exec("rm -f " + directoryPath);
			p.waitFor();
			
			return imageBytes;
	}
}
