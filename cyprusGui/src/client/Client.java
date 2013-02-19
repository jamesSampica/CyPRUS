package client;

import support.BaseMessageClient;
import support.Vehicle;
import utils.SerializationUtils;

public class Client extends BaseMessageClient {

    
    public Client(String host, int port) throws Exception {
        super(host, port);
    }

    @Override
    public void onMessage(byte[] message) {
        //System.out.printf("Message read is -> %s%n", new String(message));
        System.out.println("Read Message" );
        
        Vehicle vehicleRead = (Vehicle)SerializationUtils.bytesToVehicle(message);
        
        //Messages with packet header 0xFF are images
        if(vehicleRead.getLotNumber() == null || vehicleRead.getPlateNumber() == null){

            System.out.println("Receiving image");
            ClientController.notifyImageListeners(vehicleRead);
            
        }
        else{
            System.out.println("Receiving data");
            ClientController.notifyDataListeners(vehicleRead);
        }
    }

    
    
    
    
}
