package client;

import support.BaseMessageClient;

public class Client extends BaseMessageClient {

    
    public Client(String host, int port) throws Exception {
        super(host, port);
    }

    @Override
    public void onMessage(byte[] message) {
        //System.out.printf("Message read is -> %s%n", new String(message));
        System.out.println("Read Message" );
        
        //Messages with packet header 0xFF are images
        if(message[0] == 0xFF){

            System.out.println("Receiving image");
            ClientController.notifyImageListeners(message);
            
        }
        else{
            System.out.println("Receiving data");
            ClientController.notifyDataListeners(message);
        }
    }

    
    
    
    
}
