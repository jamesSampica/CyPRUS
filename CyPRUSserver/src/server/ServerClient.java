package server;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import support.BaseMessageClient;
import support.Vehicle;
import utils.SerializationUtils;

public class ServerClient extends BaseMessageClient {

	private Server server;
	
	public ServerClient(Socket client, Server server) {
		super(client);
		this.server = server;
	}
	
	@Override
	public void onMessage(byte[] message) {
		
		//r is image request
		if(message[0] == 'r'){
			processImageRequest(message);
		}
		//s is search
		else if(message[0] == 's'){
			processSearchRequest(message);
		}
		else if(message[0] == 'a'){
			processActiveVehiclesRequest();
		}
		//else if(message[0] == 0xFF){
		else{
			processImageReceive(message);
		}
	}
	
	private static int indexFromByteArray(byte[] b) 
	{
	    return   b[4] & 0xFF |
	            (b[3] & 0xFF) << 8 |
	            (b[2] & 0xFF) << 16 |
	            (b[1] & 0xFF) << 24;
	}

	private void processActiveVehiclesRequest(){
		this.server.processActiveVehiclesRequest(this);
	}
	
	private void processImageRequest(byte[] message){
		
		//get image index desired
		int index = indexFromByteArray(message);
		int count = 0;
		
		//Get resources folder
		Path dir = Paths.get("./plateStorage");
		try (DirectoryStream<Path> imageStream = Files.newDirectoryStream(dir)) {
			for (Path entry: imageStream) {
				if (count == index) {
					try {
                		byte[] imageFile = Files.readAllBytes(entry);
						if(this.getWriter().isConnected()){
							Vehicle toSend = new Vehicle();
							toSend.setImageBytes(imageFile);
							byte[] packetBytes = SerializationUtils.vehicleToBytes(toSend);
							writeMessage(packetBytes);
						}
					} catch (IOException e) {
						System.out.println(e.getMessage());
					}
                }
				count++;	
			}
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
	private void processSearchRequest(byte[] message){
		byte[] searchParameter = new byte[8];
		System.arraycopy(message, 1, searchParameter, 0, searchParameter.length);
		String searchString = new String(searchParameter);
		
		//Get resources folder
		Path dir = Paths.get("./plateStorage");
		try (DirectoryStream<Path> imageStream = Files.newDirectoryStream(dir)) {
			for (Path entry: imageStream) {
				String fileName = entry.getName(entry.getNameCount()-1).toString();
				fileName = fileName.substring(0, fileName.length()-5);
				if (fileName.equals(searchString.trim())) {
					try {
                		byte[] imageFile = Files.readAllBytes(entry);  
						if(this.getWriter().isConnected()){
							Vehicle toSend = new Vehicle();
							toSend.setImageBytes(imageFile);
							byte[] packetBytes = SerializationUtils.vehicleToBytes(toSend);
							writeMessage(packetBytes);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
                }
			}
		}
		catch(Exception e){
		}
	}
	private void processImageReceive(byte[] message){
		
		//receiving image from sensors
		
		ImageWriter writer = ImageIO.getImageWritersBySuffix("jpeg").next();
		ImageReader reader = ImageIO.getImageReader(writer);
		
		try {
			
	        //reader.setInput(new FileImageInputStream(new File("./plateStorage/12345.jpg")));
			reader.setInput(new MemoryCacheImageInputStream(new ByteArrayInputStream(message)));
			
	        IIOMetadata imageMetadata = reader.getImageMetadata(0);
            Element tree = (Element) imageMetadata.getAsTree("javax_imageio_jpeg_image_1.0");
            NodeList comNL = tree.getElementsByTagName("com");
            
            if (comNL.getLength() != 0) {
                IIOMetadataNode comNode = (IIOMetadataNode) comNL.item(0);
                byte[] plateBytes = (byte[])comNode.getUserObject();
                String nodeData = new String(plateBytes);
                String[] plateAndLot = nodeData.split(" ");
                
        		if(plateAndLot.length >= 2){
        			Vehicle capturedVehicle = new Vehicle(plateAndLot[0], plateAndLot[1]);
                    capturedVehicle.setImageBytes(message);
                    this.server.processReceivedData(capturedVehicle);
        		}
                
            }
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			reader.dispose();
			writer.dispose();
			
		}
	}
}
