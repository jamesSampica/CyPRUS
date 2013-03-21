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
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import support.Packet;
import support.Vehicle;
import utils.SerializationUtils;

/**
 *
 * @author James Sampica
 * This class acts as a controller between the GUI and the client
 * socket. All commands that come in or go out are intended to go through this
 * class. This class uses static methods so that any GUI component can send and
 * receive messages
 */
public class ClientController {

    private static Client client;
    private static ArrayList<PacketListener> packetListeners = new ArrayList<>();
    private static int serverPort;
    private static String serverIP;
    
    
    private ClientController() {
    }

    /**
     * Should be called prior to any network calls. Sets up the client using the
     * stored server settings such that messages can be sent and received.
     */
    public static void setupFromFileSettings() {
        readSettingsFromFile();
        
        if(serverPort > 0 && !serverIP.isEmpty()){
             setupClientFromSettings();
        }
    }
    
     /**
     * Should be called prior to any network calls. Sets up the client using the
     * stored server settings such that messages can be sent and received.
     */
    public static void setupFromArguments(String ip, int port) {
        if (port > 0 && !ip.isEmpty()) {
            serverIP = ip;
            serverPort = port;
            setupClientFromSettings();
        }
    }

    /**
     * Gets the server port that the client is connected to
     * @return the port number
     */
    public static int getServerPort(){
        return serverPort;
    }
    
    /**
     * Gets the server IP that the client is connected to
     * @return the server IP string
     */
    public static String getServerIP(){
        return serverIP;
    }
    
    private static void readSettingsFromFile(){
        FileReader file = null;
        BufferedReader reader = null;
        
        try {
            file = new FileReader("cyprusSettings.txt");
            reader = new BufferedReader(file);
            serverPort = Integer.parseInt(reader.readLine());
            serverIP = reader.readLine();
        } catch (FileNotFoundException e) {
            //System.out.println("Settings not found: " + e.getMessage());
            java.util.logging.Logger.getLogger(ClientController.class.getName()).log(java.util.logging.Level.WARNING, "Settings not found: {0}", e.getMessage());
        } catch (IOException e) {
            //System.out.println("IO Error occurred: " + e.getMessage());
            java.util.logging.Logger.getLogger(ClientController.class.getName()).log(java.util.logging.Level.WARNING, "IO Error occurred: {0}", e.getMessage());
        } catch (NumberFormatException e) {
            //System.out.println("Couldn't parse settings port as int: " + e.getMessage());
            java.util.logging.Logger.getLogger(ClientController.class.getName()).log(java.util.logging.Level.WARNING, "Couldn''t parse settings port as int: {0}", e.getMessage());
        } catch (Exception e) {
            //System.out.println("Generic Exception occurred: " + e.getMessage());
            java.util.logging.Logger.getLogger(ClientController.class.getName()).log(java.util.logging.Level.WARNING, "Generic Exception occurred: {0}", e.getMessage());
        } finally {
            if (file != null) {
                try {
                    reader.close();
                    file.close();
                } catch (IOException e) {
                    //System.out.println("IO Error occurred: " + e.getMessage());
                    java.util.logging.Logger.getLogger(ClientController.class.getName()).log(java.util.logging.Level.WARNING, "IO Error occurred: {0}", e.getMessage());
                }
            }
        }
    }
    
    /**
     * Writes the port setting to the settings file
     *
     * @param newPort desired port to set
     */
    private static void writeSettingsToFile() {
        FileWriter output = null;
        BufferedWriter writer = null;
        try {
            output = new FileWriter("cyprusSettings.txt");
            writer = new BufferedWriter(output);
            writer.write(String.valueOf(serverPort));
            writer.newLine();
            writer.write(serverIP);
        } catch (IOException e) {
            //System.out.println(e.getMessage());
            java.util.logging.Logger.getLogger(ClientController.class.getName()).log(java.util.logging.Level.WARNING, e.getMessage());
        } finally {
            if (output != null) {
                try {
                    writer.close();
                    output.close();
                } catch (IOException e) {
                    java.util.logging.Logger.getLogger(ClientController.class.getName()).log(java.util.logging.Level.WARNING, e.getMessage());
                }
            }
        }
    }

    /**
     * Returns whether the client socket has an open connected to the server
     *
     * @return true if connected, false otherwise
     */
    public static boolean isConnected() {
        if (client == null) {
            return false;
        }

        return client.getSocket().isConnected();
    }

    /**
     * Disconnects the current client and socket connection, overwriting the connection
     * settings to the settings file
     */
    public static void disconnectClient() {
        if (client != null) {
            writeSettingsToFile();
            client.disconnect();
        }
    }

    private static void setupClientFromSettings() { 
        try {
            client = new Client(serverIP, serverPort);
        } catch (Exception ex) {
            Logger.getLogger(ClientController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Registers an object implementing PacketListener to listen for incoming
     * Packets. All data received is implemented through listening to this
     *
     * @param listener the object that wants to listen for network activity
     */
    public static void registerDataListener(PacketListener listener) {
        packetListeners.add(listener);
    }

    /**
     * Unregisters an object implementing PacketListener that listens for
     * incoming Packets. Will permanently remove it from receiving incoming data
     *
     * @param listener the object that wants to disconnect data service
     */
    public static void unRegisterDataListener(PacketListener listener) {
        packetListeners.remove(listener);
    }

    //test code
    public static void test() {
        // Packet p = new Packet(Packet.ActiveVehiclesCommand);
        client.writeMessage("test".getBytes());
    }

    /**
     * Sends a search request given a key to search to the server if a
     * connection exists. Data sent back comes streaming to PacketListeners.
     *
     * @param searchKey the key to perform a search on
     */
    public static void searchRequest(String searchKey) {

        if (!isConnected()) {
            return;
        }

        Packet toSend = new Packet(Packet.SearchCommand);
        toSend.setData(searchKey);
        byte[] bytesToSend = SerializationUtils.packetToBytes(toSend);

        client.writeMessage(bytesToSend);
    }

    
    /**
     * Sends a request to stream this client all vehicles that have been recently
     * tagged as violations
     */
    public static void recentVehiclesRequest(){
        if (!isConnected()) {
            return;
        }
        
        Packet toSend = new Packet(Packet.RecentVehiclesCommand);
        byte[] bytesToSend = SerializationUtils.packetToBytes(toSend);

        client.writeMessage(bytesToSend);
    }
    
    
    /**
     * Sends a server request to store a new platenumber and lotnumber combination
     * with a date pass. Any vehicles that enter with this combination are ignored
     * by the server until the expiration date
     * @param platenumber the vehicles platenumber
     * @param lotnumber the vehicles's lotnumber
     * @param expiration the desired expiration date of the pass
     */
    public static void sendDatePassRequest(String platenumber, String lotnumber, Date expiration){
        Packet toSend = new Packet(Packet.InsertDatePassCommand);
        
        Vehicle v = new Vehicle();
        v.setLotNumber(lotnumber);
        v.setPlateNumber(platenumber);
        v.setGraceEndDate(expiration);
        toSend.setData(v);
        
        byte[] bytesToSend = SerializationUtils.packetToBytes(toSend);

        client.writeMessage(bytesToSend);
    }
    
    /**
     * Sends a server request to store a new platenumber and lotnumber combination
     * with a date pass. Any vehicles that enter with this combination are ignored
     * by the server until the expiration date
     * @param platenumber the vehicles platenumber
     * @param lotnumber the vehicles's lotnumber
     * @param expiration the desired expiration date of the pass
     */
     public static void sendTimePassRequest(String platenumber, String lotnumber, int hours){
        Packet toSend = new Packet(Packet.InsertTimePassCommand);
       
        Vehicle v = new Vehicle();
        v.setLotNumber(lotnumber);
        v.setPlateNumber(platenumber);
        v.setTimePassAmount(hours);
        toSend.setData(v);
        
        byte[] bytesToSend = SerializationUtils.packetToBytes(toSend);

        client.writeMessage(bytesToSend);
    }
     
    /**
     * Sends a request to the server to stream all active vehicles to this
     * client. Data sent back comes streaming to PacketListeners
     */
    public static void activeVehiclesRequest() {

        if (!isConnected()) {
            return;
        }

        Packet toSend = new Packet(Packet.ActiveVehiclesCommand);
        byte[] bytesToSend = SerializationUtils.packetToBytes(toSend);

        client.writeMessage(bytesToSend);
    }

    /**
     * Notifies all PacketListeners that the given data has been received and sends
     * it to them
     * @param message the message received to be sent to PacketListeners
     */
    public static void notifyPacketListeners(Packet message) {
        for (PacketListener vl : packetListeners) {
            vl.onPacketReceived(message);
        }
    }
    
    
}
