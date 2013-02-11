package support;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.MemoryCacheImageInputStream;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author James
 */
public class ImageUtils {

    public static String[] getPlateAndLotFromBytes(byte[] imageData) {
        
        ImageWriter writer = ImageIO.getImageWritersBySuffix("jpeg").next();
        ImageReader reader = ImageIO.getImageReader(writer);
             
        String[] nodeDataSplit = null;
        try {
            reader.setInput(new MemoryCacheImageInputStream(new ByteArrayInputStream(imageData)));

            IIOMetadata imageMetadata = reader.getImageMetadata(0);
            Element tree = (Element) imageMetadata.getAsTree("javax_imageio_jpeg_image_1.0");
            NodeList comNL = tree.getElementsByTagName("com");
            
            if (comNL.getLength() != 0) {
                IIOMetadataNode comNode = (IIOMetadataNode) comNL.item(0);
                byte[] plateBytes = (byte[])comNode.getUserObject();
                String nodeData = new String(plateBytes);
                
                //String 0 = Plate
                //String 1 = Lot
                nodeDataSplit = nodeData.split(" ");
            }

        } catch (IOException e) {
            
        } finally {
            reader.dispose();
            writer.dispose(); 
        }
        
        return nodeDataSplit;
    }
}
