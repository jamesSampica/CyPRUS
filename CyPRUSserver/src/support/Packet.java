package support;

import java.io.Serializable;

/**
 * A serializable packet class used to communicate data objects between server and client
 * @author James Sampica
 *
 */
public class Packet implements Serializable {

	private static final long serialVersionUID = 8357725521248588829L;

	public static int SearchCommand = 0;
	public static int ActiveVehiclesCommand = 1;
	public static int RecentVehiclesCommand = 2;
	public static int CapturedVehicleCommand = 3;
	public static int InsertDatePassCommand = 4;
	public static int InsertTimePassCommand = 5;
	
	private Object data;
	
	private int command;
	
	/**
	 * Creates a new packet object given a command
	 * @param command the command (0-5) for the packet to have
	 */
	public Packet(int command){
		if(command > 5 || command < 0){
			throw new RuntimeException("Command doesnt exist");
		}
		
		this.command = command;
	}
	
	/**
	 * Gets the command associated with this packet
	 * @return the command associated with this packet
	 */
	public int getCommand(){
		return command;
	}
	
	/**
	 * Gets the data associated with this packet
	 * Since this takes the form of Object, many different types of data are
	 * able to be stored
	 * @return the data associated with this packet
	 */
	public Object getData(){
		return data;
	}
	
	/**
	 * Sets the data associated with this packet
	 * @param data the new data to set
	 */
	public void setData(Object data){
		this.data = data;
	}
		
}
