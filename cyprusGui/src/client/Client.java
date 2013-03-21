package client;

import support.BaseMessageClient;
import support.Packet;
import utils.SerializationUtils;

/**
 * Serves as the model for client communication to the server
 * @author James Sampica
 */
public class Client extends BaseMessageClient {

    /**
     * Constructs a new communication client
     * @param host the hostname or ip
     * @param port the desired port
     * @throws Exception thrown if a connection cannot be established
     */
    public Client(String host, int port) throws Exception {
        super(host, port);
    }

    /**
     * Handles the reception of messages from the server
     */
    @Override
    public void onMessage(byte[] message) {
        Packet packetRead = SerializationUtils.bytesToPacket(message);

        //System.out.println("Receiving data");
        ClientController.notifyPacketListeners(packetRead);
    }

    
    
    
    
}
