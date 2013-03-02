/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package interfaces;

import support.Packet;

/**
 *
 * @author James
 */
public interface PacketListener {
    public void onPacketReceived( Packet vehicle );
}
