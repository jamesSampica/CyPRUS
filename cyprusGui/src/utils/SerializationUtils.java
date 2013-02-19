package utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import support.Vehicle;

public class SerializationUtils {
	
	
	public static byte[] vehicleToBytes(Object object){
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		byte[] vehicleBytes = null;
		try {
		  out = new ObjectOutputStream(bos);   
		  out.writeObject(object);
		  vehicleBytes = bos.toByteArray();
		  out.close();
		  bos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return vehicleBytes;
	}

	
	public static Object bytesToVehicle(byte[] objectBytes){
		ByteArrayInputStream bis = new ByteArrayInputStream(objectBytes);
		ObjectInput in = null;
		Vehicle o = null;
		try {
		  in = new ObjectInputStream(bis);
		  o = (Vehicle) in.readObject(); 
		  bis.close();
		  in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return o;
	}

}
