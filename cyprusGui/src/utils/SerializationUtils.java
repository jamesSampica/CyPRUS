package utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import support.Packet;

/**
 * This class defines methods related to object serialization
 * @author James Sampica
 *
 */
public class SerializationUtils {
	
	/**
	 * Converts a Packet.java object to a byte[] array
	 * @param object the Packet object to convert
	 * @return the serialized object if successful, null otherwise
	 */
	public static byte[] packetToBytes(Packet object){
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		byte[] packetBytes = null;
		try {
		  out = new ObjectOutputStream(bos);   
		  out.writeObject(object);
		  packetBytes = bos.toByteArray();
		  out.close();
		  bos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			Logger.getLogger(SerializationUtils.class.getName()).log(Level.WARNING, "packetToBytes IOException: {0}", e.getMessage());
		}
		
		return packetBytes;
	}

	/**
	 * Converts a byte[] array to a Packet.java object
	 * @param objectBytes the Packet object bytes to convert
	 * @return the object form of the serialized bytes
	 */
	public static Packet bytesToPacket(byte[] objectBytes){
		ByteArrayInputStream bis = new ByteArrayInputStream(objectBytes);
		ObjectInput in = null;
		Packet o = null;
		try {
		  in = new ObjectInputStream(bis);
		  o = (Packet) in.readObject(); 
		  bis.close();
		  in.close();
		} catch (IOException e) {
			//System.out.println("Possible problem converting bytes vehicle");
			//Logger.getLogger(SerializationUtils.class.getName()).log(Level.WARNING,  "bytesToPacket IOException: " + e.getMessage());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			Logger.getLogger(SerializationUtils.class.getName()).log(Level.SEVERE, "bytesToPacket ClassNotFoundException: {0}", e.getMessage());
		}
		
		return o;
	}

}
