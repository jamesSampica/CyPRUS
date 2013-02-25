/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import support.Vehicle;
import support.VehicleListener;

/**
 *
 * @author James
 */
public class ClientController {
    
    private static Client client;
    
    private static ArrayList<VehicleListener> imageListeners;
    private static ArrayList<VehicleListener> dataListeners;
   
    
    private ClientController(){

    }

    public static void setup(){
        setupClientFromSettings(-1);
        imageListeners = new ArrayList<>();
        dataListeners = new ArrayList<>();
    }
    
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
        
    public static void registerDataListener(VehicleListener listener){
        dataListeners.add(listener);
    }
            
    public static void unRegisterDataListener(VehicleListener listener){
        dataListeners.remove(listener);
    }
    
    //test code
    public static void test(){
        client.writeMessage("test".getBytes());
    }
    
    public static void searchRequest(String searchKey){
        
        if(!isConnected()){
            return;
        }
        
        byte[] searchKeyBytes = searchKey.getBytes();
        byte[] packet = new byte[searchKeyBytes.length+2];
        
        packet[0] = 's';
        packet[1] = ' ';
        
        System.arraycopy(searchKeyBytes, 0, packet, 2, searchKeyBytes.length);
        client.writeMessage(packet);
    }
    
    public static void activeVehiclesRequest(){
        
        if(!isConnected()){
            return;
        }
        
        byte[] packet = new byte[1];
        packet[0] = 'a';
        client.writeMessage(packet);
    }
    
    public static void notifyDataListeners(Vehicle vehicle){
        System.out.println("Notifying data listeners");
        for(VehicleListener vl: dataListeners){
            vl.onVehicleMessage(vehicle);
        }
    }
}
