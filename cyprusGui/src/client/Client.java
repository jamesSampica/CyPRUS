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
        Vehicle vehicleRead = (Vehicle)SerializationUtils.bytesToVehicle(message);

        System.out.println("Receiving data");
        ClientController.notifyDataListeners(vehicleRead);
    }

    
    
    
    
}
