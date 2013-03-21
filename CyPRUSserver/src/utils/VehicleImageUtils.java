package utils;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDescriptor;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import support.Vehicle;

/**
 * Contains utility methods for working with Vehicle Images
 * @author James Sampica
 */
public class VehicleImageUtils {

	/**
	 * Parses the plate and lotnumber from a jpg image in byte[] form
	 * This method is expensive so use it sparingly
	 * 
	 * @param imageData the jpg image data containing the vehicle data
	 * @return a string array containing the platenumber in index 0 and lotnumber in index 1
	 */
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
				
				//Replace all non-alphanumeric characters
				nodeDataSplit[0] = nodeDataSplit[0].replaceAll("[^A-Za-z0-9]", "");
				nodeDataSplit[1] = nodeDataSplit[1].replaceAll("[^A-Za-z0-9]", "");
			}
			
		} catch (ImageProcessingException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			Logger.getLogger(VehicleImageUtils.class.getName()).log(Level.WARNING,  "getPlateAndLotFromBytes ImageProcessingException: " + e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			Logger.getLogger(VehicleImageUtils.class.getName()).log(Level.WARNING,  "getPlateAndLotFromBytes IOException: " + e.getMessage());
		}
		
		return nodeDataSplit;
	}
    
	/**
	 * Reads from the disk the image bytes that correspond to a violation vehicle's platenumber and lotnumber
	 * and sets them in the vehicles setImageBytes()
	 * @param v The vehicle to get image data for
	 * @return true if vehicle information was set correctly, false otherwise
	 */
    public static boolean getImageFileBytesForVehicle(Vehicle v) {
    	
    	try {
    		String directoryPath = "./plateStorage/violationplates/";
    		String imagePathString = v.getPlateNumber()+v.getLotNumber()+(v.getGraceEndDate().getTime()/1000);
    		Path imagePath = Paths.get(directoryPath+imagePathString+".jpg");
			v.setImageBytes(Files.readAllBytes(imagePath));
		} catch (Exception e) {
			return false;
		}
    	
    	return true;
    }
    
    /**
     * Attempts to save a vehicle's image bytes to the violation directory
     * @param v the vehicle that contains the image bytes to save
     * @return true if the image was saved successfully, false otherwise
     */
    public static boolean saveImageBytesForVehicle(Vehicle v){
    	
    	try {
    	    // save image
    		String directoryPath = "./plateStorage/violationplates/";
    		String imagePath = v.getPlateNumber()+v.getLotNumber()+(v.getGraceEndDate().getTime()/1000);
    		
    		InputStream in = new ByteArrayInputStream(v.getImageBytes());
    	    BufferedImage bi = ImageIO.read(in);
    	    File outputfile = new File(directoryPath + imagePath + ".jpg");
    	    ImageIO.write(bi, "jpg", outputfile);
    	} catch (IOException e) {
    	    return false;
    	}
    	
    	return true;
    }
    
    public static boolean getProblemImageFileBytesForVehicle(Vehicle v) {
    	
    	try {
    		String directoryPath = "./plateStorage/problemplates/" + v.getPlateNumber() + "/";
    		String imagePathString = v.getPlateNumber()+v.getLotNumber()+(v.getGraceEndDate().getTime()/1000);
    		
    		Path imagePath = Paths.get(directoryPath+ imagePathString +".jpg");
			v.setImageBytes(Files.readAllBytes(imagePath));
		} catch (Exception e) {
			return false;
		}
    	
    	return true;
    }
    
    public static boolean saveProblemImageBytesForVehicle(Vehicle v){
    	
    	try {
    	    // save image
    		String directoryPath = "./plateStorage/problemplates/" + v.getPlateNumber() + "/"; 		
    		InputStream in = new ByteArrayInputStream(v.getImageBytes());
    	    BufferedImage bi = ImageIO.read(in);
    	    File outputfile = new File(directoryPath+v.getPlateNumber()+v.getEntryDate().getTime()+".jpg");
    	    ImageIO.write(bi, "jpg", outputfile);
    	} catch (IOException e) {
    	    return false;
    	}
    	
    	return true;
    }
}
