package client;

import support.BaseMessageClient;
import support.Packet;
import utils.SerializationUtils;

public class Client extends BaseMessageClient {

    
    public Client(String host, int port) throws Exception {
        super(host, port);
    }

    @Override
    public void onMessage(byte[] message) {
        Packet packetRead = SerializationUtils.bytesToPacket(message);

        System.out.println("Receiving data");
        ClientController.notifyDataListeners(packetRead);
    }

    
    
    
    
}
