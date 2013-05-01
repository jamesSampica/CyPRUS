package support;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * A serializable class that defines what it is to be a vehicle in a lot
 * This is one class that is commonly sent via Packet
 * 
 * @author James Sampica
 *
 */
public class Vehicle implements Serializable {

	private static final long serialVersionUID = 6026628041263604534L;

	/**
	 * The grace time allowed to vehicles entering lots
	 */
	public static int GraceQuantumMillis = 10000;

	private String plateNumber;
	private String lotNumber;
	private Date entryDate;
	private Date graceEndDate;
	private Integer timePassAmount;
	private byte[] imageBytes;

	/**
	 * Creates a new vehicle, setting the entrydate as "now" and the grace end date as "now"+GraceQuantumMillis
	 */
	public Vehicle() {
		this.entryDate = new Date(System.currentTimeMillis());

		// Set the grace period end date to be entry date + grace period time amount
		this.graceEndDate = new Date(System.currentTimeMillis() + GraceQuantumMillis);
	}

	/**
	 * Creates a new vehicle with specifiec plate and lotnumber, also sets the 
	 * entrydate as "now" and the grace end date as "now"+GraceQuantumMillis
	 * 
	 * @param plateNumber the plate number for this vehicle
	 * @param lotNumber the lot number for this vehicle
	 */
	public Vehicle(String plateNumber, String lotNumber) {
		this.plateNumber = plateNumber;
		this.lotNumber = lotNumber;

		this.entryDate = new Date(System.currentTimeMillis());
		// Set the grace period end date to be entry date + grace period time
		// amount
		this.graceEndDate = new Date(System.currentTimeMillis() + GraceQuantumMillis);

	}

	/**
	 * Sets the plate number for this object
	 * @param plate number the new plate number
	 */
	public void setPlateNumber(String plateNumber) {
		this.plateNumber = plateNumber;
	}

	/**
	 * Gets the plate number for this object
	 * @return the plate number for this object
	 */
	public String getPlateNumber() {
		return plateNumber;
	}

	/**
	 * Sets the lotNumber for this object
	 * @param lotNumber the new lotnumber
	 */
	public void setLotNumber(String lotNumber) {
		this.lotNumber = lotNumber;
	}

	/**
	 * Gets the lot number for this object
	 * @return the lot number for this object
	 */
	public String getLotNumber() {
		return lotNumber;
	}

	/**
	 * sets the entry date for this object
	 * @param entryDate the new entry date
	 */
	public void setEntryDate(Date entryDate){
		this.entryDate = entryDate;
	}
	
	/**
	 * gets the entry date for this object
	 * @return the entry date for this object
	 */
	public Date getEntryDate() {
		return entryDate;
	}

	/**
	 * sets the grace end Date for this object
	 * @param graceEndDate the new grace end date
	 */
	public void setGraceEndDate(Date graceEndDate){
		this.graceEndDate = graceEndDate;
	}
	
	/**
	 * gets the grace end date for this object
	 * @return the grace end date
	 */
	public Date getGraceEndDate() {
		return graceEndDate;
	}

	/**
	 * gets the image in byte form for this vehicle
	 * @return the image in bytes
	 */
	public byte[] getImageBytes() {
		return imageBytes;
	}

	/**
	 * sets the image bytes for this vehicle
	 * @param imageBytes the image in byte form
	 */
	public void setImageBytes(byte[] imageBytes) {
		this.imageBytes = imageBytes;
	}

	/**
	 * sets the amount of pass time (in hours) that this vehicle has
	 * @param timePassAmount the new amount of time
	 */
	public void setTimePassAmount(int timePassAmount){
		this.timePassAmount = timePassAmount;
	}
	
	/**
	 * gets the amount of pass time (in hours) that this vehicle has
	 * @return the amount of pass time
	 */
	public int getTimePassAmount(){
		return timePassAmount;
	}
	
	@Override
	public String toString() {
		return plateNumber + lotNumber;
	}

    @Override
    public boolean equals(Object o) {

    	if(o == null){
    		return false;
    	}
    	
    	if(o == this){
    		return true;
    	}
    	
        if (o.getClass() != Vehicle.class) {
            return false;
        }

        return o.hashCode() == this.hashCode();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + Objects.hashCode(this.plateNumber);
        hash = 83 * hash + Objects.hashCode(this.lotNumber);
        return hash;
    }
}
