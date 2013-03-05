package utils;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

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
    	
    	try {
    		Path imagePath = Paths.get(v.getPlateNumber()+v.getLotNumber()+v.getGraceEndDate().getTime()+".jpg");
			v.setImageBytes(Files.readAllBytes(imagePath));
		} catch (Exception e) {
			return false;
		}
    	
    	return true;
    }
    
    public static boolean saveImageBytesForVehicle(Vehicle v){
    	
    	try {
    	    // retrieve image
    		InputStream in = new ByteArrayInputStream(v.getImageBytes());
    	    BufferedImage bi = ImageIO.read(in);
    	    File outputfile = new File(v.getPlateNumber()+v.getLotNumber()+v.getEntryDate().getTime()+".jpg");
    	    ImageIO.write(bi, "jpg", outputfile);
    	} catch (IOException e) {
    	    return false;
    	}
    	
    	return true;
    }
}
