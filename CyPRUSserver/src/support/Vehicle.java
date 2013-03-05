package support;

import java.io.Serializable;
import java.util.Date;

public class Vehicle implements Serializable {

	private static final long serialVersionUID = 6026628041263604534L;

	public static int GraceQuantumMillis = 10000;

	private String plateNumber;
	private String lotNumber;
	private Date entryDate;
	private Date graceEndDate;
	private byte[] imageBytes;

	public Vehicle() {
		this.entryDate = new Date(System.currentTimeMillis());

		// Set the grace period end date to be entry date + grace period time
		// amount
		this.graceEndDate = new Date(System.currentTimeMillis() + GraceQuantumMillis);
	}

	public Vehicle(String plateNumber, String lotNumber) {
		this.plateNumber = plateNumber;
		this.lotNumber = lotNumber;

		this.entryDate = new Date(System.currentTimeMillis());

		// Set the grace period end date to be entry date + grace period time
		// amount
		this.graceEndDate = new Date(System.currentTimeMillis() + GraceQuantumMillis);

	}

	public void setPlateNumber(String plateNumber) {
		this.plateNumber = plateNumber;
	}

	public String getPlateNumber() {
		return plateNumber;
	}

	public void setLotNumber(String lotNumber) {
		this.lotNumber = lotNumber;
	}

	public String getLotNumber() {
		return lotNumber;
	}

	public void setEntryDate(Date entryDate){
		this.entryDate = entryDate;
	}
	
	public Date getEntryDate() {
		return entryDate;
	}

	public void setGraceEndDate(Date graceEndDate){
		this.graceEndDate = graceEndDate;
	}
	
	public Date getGraceEndDate() {
		return graceEndDate;
	}

	public byte[] getImageBytes() {
		return imageBytes;
	}

	public void setImageBytes(byte[] imageBytes) {
		this.imageBytes = imageBytes;
	}

	@Override
	public String toString() {
		return plateNumber + lotNumber;
	}

	@Override
	public boolean equals(Object o) {
		if (o != null && o.toString().equals(this.toString())) {
			return true;
		}

		return false;
	}
}
