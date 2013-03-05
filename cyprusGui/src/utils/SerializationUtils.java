package utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import support.Packet;

public class SerializationUtils {
	
	
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
			e.printStackTrace();
		}
		
		return packetBytes;
	}

	
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
			System.out.println("Possible problem converting bytes vehicle");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return o;
	}

}
