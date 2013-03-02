package support;

import java.io.Serializable;

public class Packet implements Serializable {

	private static final long serialVersionUID = 8357725521248588829L;

	public static int SearchCommand = 0;
	public static int ActiveVehiclesCommand = 1;
	public static int RecentVehiclesCommand = 2;
	public static int PingCommand = 3;
	public static int CapturedVehicleCommand = 4;
	
	private int command;
	private String searchString;
	private Vehicle vehicle;
	
	public Packet(int command){
		if(command > 4 || command < 0){
			throw new RuntimeException("Command doesnt exist");
		}
		
		this.command = command;
	}
	
	public int getCommand(){
		return command;
	}
	
	public Vehicle getVehicle(){
		return vehicle;
	}
	
	public void setVehicle(Vehicle vehicle){
		this.vehicle = vehicle;
	}
	
	public String getSearchString(){
		return searchString;
	}
	
	public void setSearchString(String searchString){
		this.searchString = searchString;
	}
}
