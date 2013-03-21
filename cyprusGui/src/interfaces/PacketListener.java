/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package interfaces;

import support.Packet;

/**
 * A general interface for listening to messages sent from the server. To use this
 * interface, implement the interface on the class desired and add that class
 * to the ClientController.
 * @author James
 */
public interface PacketListener {
    public void onPacketReceived( Packet vehicle );
}
