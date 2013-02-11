package server;

import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import support.BaseMessageClient;

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
							writeMessage(imageFile);
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
		//s is search
		else if(message[0] == 's'){
			
			byte[] searchParameter = new byte[8];
			System.arraycopy(message, 1, searchParameter, 0, searchParameter.length);
			String searchString = new String(searchParameter);
			
			//Get resources folder
			Path dir = Paths.get("./plateStorage");
			try (DirectoryStream<Path> imageStream = Files.newDirectoryStream(dir)) {
				for (Path entry: imageStream) {
					String plateString = entry.getName(entry.getNameCount()-1).toString();
            		plateString = FilenameUtils.removeExtension(plateString);
					if (plateString.equals(searchString.trim())) {
						try {
                    		byte[] imageFile = Files.readAllBytes(entry);  
							writeMessage(imageFile);
						} catch (IOException e) {
							e.printStackTrace();
						}
                    }
				}
			}
			catch(Exception e){
			}
		}
		else{
			//receiving image from sensors
			
			ImageWriter writer = ImageIO.getImageWritersBySuffix("jpeg").next();
			ImageReader reader = ImageIO.getImageReader(writer);
			
			try {
				//Path img = Paths.get("./plateStorage/12345.jpg");
				//byte[] imageFile = Files.readAllBytes(img);

		        //reader.setInput(new FileImageInputStream(new File("./plateStorage/12345.jpg")));
				reader.setInput(new MemoryCacheImageInputStream(new ByteArrayInputStream(message)));
				
		        IIOMetadata imageMetadata = reader.getImageMetadata(0);
	            Element tree = (Element) imageMetadata.getAsTree("javax_imageio_jpeg_image_1.0");
	            NodeList comNL = tree.getElementsByTagName("com");
	            
	            
	            if (comNL.getLength() != 0) {
	                IIOMetadataNode comNode = (IIOMetadataNode) comNL.item(0);
	                byte[] plateBytes = (byte[])comNode.getUserObject();
	                String nodeData = new String(plateBytes);
	                
	                //String 0 = Plate
	                //String 1 = Lot
	                String[] nodeDataSplit = nodeData.split(" ");
	                this.server.processReceivedData(nodeDataSplit);
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
	
	private static int indexFromByteArray(byte[] b) 
	{
	    return   b[4] & 0xFF |
	            (b[3] & 0xFF) << 8 |
	            (b[2] & 0xFF) << 16 |
	            (b[1] & 0xFF) << 24;
	}

}
