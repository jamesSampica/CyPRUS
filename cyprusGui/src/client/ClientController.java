/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import interfaces.PacketListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import support.Packet;
import utils.SerializationUtils;

/**
 *
 * @author James
 * This class acts as a controller between the GUI and the client socket.
 * All commands that come in or go out are intended to go through this class.
 * This class uses static methods so that any GUI component can send and receive
 * messages
 */
public class ClientController {
    
    private static Client client;
    
    private static ArrayList<PacketListener> dataListeners;
   
    
    private ClientController(){

    }

    /**
     * Should be called prior to any network calls. Sets up the client using
     * the stored server settings such that messages can be sent and received.
     */
    public static void setup(){
        setupClientFromSettings(-1);
        dataListeners = new ArrayList<>();
    }
    
    
    /**
     * Writes the port setting to the settings file
     * @param newPort desired port to set 
     */
    public static void setPortSettings(int newPort){
        if(client != null){
            client.disconnect();
        }
            
        setupClientFromSettings(newPort);
        
        FileWriter output = null;
        BufferedWriter writer = null;
        try {
            output = new FileWriter("cyprusSettings.txt");
            writer = new BufferedWriter(output);
            writer.write(String.valueOf(newPort));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            if (output != null) {
                try {
                    writer.close();
                    output.close(); 
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
    
    /**
     * Retrieves the port setting from the settings file
     * @return settings level port
     */
    public static int getPortSettings(){
        if(client != null){
            return client.getSocket().getPort();
        }
        FileReader file = null;
        BufferedReader reader = null;
        int port = -1;
        try {
            file = new FileReader("cyprusSettings.txt");
            reader = new BufferedReader(file);
            port = Integer.parseInt(reader.readLine());
        } catch (FileNotFoundException e) {
            System.out.println("Settings not found: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO Error occurred: " + e.getMessage());
        } catch(NumberFormatException e){
            System.out.println("Couldn't parse settings port as int: " + e.getMessage());
        } catch(Exception e){
            System.out.println("Generic Exception occurred: " + e.getMessage());
        } finally {
            if (file != null) {
                try {
                    reader.close();
                    file.close();
                } catch (IOException e) {
                    System.out.println("IO Error occurred: " + e.getMessage());
                }
            }
        }
        
        return port;
    }
    
    /**
     * 
     * @return 
     */
    public static boolean isConnected(){
        if(client == null){
            return false;
        }
        
        return client.getSocket().isConnected();
    }
    
    public static void disconnectClient(){
        if(client != null){
            client.disconnect();
        }
    }
    
    private static void setupClientFromSettings(int port) {
        FileReader file = null;
        BufferedReader reader = null;
        try {
            if(port > 0){
                client = new Client("localhost", port);
                return;
            }
            file = new FileReader("cyprusSettings.txt");
            reader = new BufferedReader(file);
            port = Integer.parseInt(reader.readLine());
            client = new Client("localhost", port);
            
        } catch (FileNotFoundException e) {
            System.out.println("Settings not found: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO Error occurred: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Couldn't parse settings port: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Generic Exception occurred: " + e.getMessage());
        } finally {
            if (file != null) {
                try {
                    reader.close();
                    file.close();
                } catch (IOException e) {
                    System.out.println("IO Error occurred: " + e.getMessage());
                }
            }
        }
    }
        
    public static void registerDataListener(PacketListener listener){
        dataListeners.add(listener);
    }
            
    public static void unRegisterDataListener(PacketListener listener){
        dataListeners.remove(listener);
    }
    
    //test code
    public static void test(){
        // Packet p = new Packet(Packet.ActiveVehiclesCommand);
        client.writeMessage("test".getBytes());
    }
    
    public static void searchRequest(String searchKey){
        
        if(!isConnected()){
            return;
        }
        
        Packet toSend = new Packet(Packet.SearchCommand);
        toSend.setSearchString(searchKey);
        byte[] bytesToSend = SerializationUtils.packetToBytes(toSend);
        
        client.writeMessage(bytesToSend);
    }
    
    public static void activeVehiclesRequest(){
        
        if(!isConnected()){
            return;
        }
        
        Packet toSend = new Packet(Packet.ActiveVehiclesCommand);
        byte[] bytesToSend = SerializationUtils.packetToBytes(toSend);
        
        client.writeMessage(bytesToSend);
    }
    
    public static void notifyDataListeners(Packet message){
        System.out.println("Notifying data listeners");
        for(PacketListener vl: dataListeners){
            vl.onPacketReceived(message);
        }
    }
}
