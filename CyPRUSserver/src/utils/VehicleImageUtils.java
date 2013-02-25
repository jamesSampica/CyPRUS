package utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDescriptor;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import support.Vehicle;

/**
 *
 * @author James
 */
public class VehicleImageUtils {

	public static String[] getPlateAndLotFromBytes(byte[] imageData) {
		
		String[] nodeDataSplit = null;
		
		try{

			BufferedInputStream is = new BufferedInputStream(new ByteArrayInputStream(imageData), imageData.length);
			Metadata metadata = ImageMetadataReader.readMetadata(is, true);

			ExifSubIFDDirectory exifdir = metadata.getDirectory(ExifSubIFDDirectory.class);
			ExifSubIFDDescriptor exifdes = new ExifSubIFDDescriptor(exifdir);

			if(exifdes.getUserCommentDescription() != null){
				String nodeData = new String(exifdes.getUserCommentDescription().toString());

				nodeDataSplit = nodeData.split(" ");
			}
			
		} catch (ImageProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return nodeDataSplit;
	}
    
    public static boolean getImageFileBytesForVehicle(Vehicle v) {
    	
    	
    	
    	return true;
    }
    
    public static boolean saveImageBytesForVehicle(Vehicle v){
    	
    	return true;
    }
}
