package support;

import java.io.Serializable;

public class Vehicle implements Serializable {

	private static final long serialVersionUID = 6026628041263604534L;

	public static int GraceQuantumMillis = 10000;

	private String plateNumber;
	private String lotNumber;
	private long entryDate;
	private long graceEndDate;
	private byte[] imageBytes;

	public Vehicle() {
		this.entryDate = System.currentTimeMillis();

		// Set the grace period end date to be entry date + grace period time
		// amount
		this.graceEndDate = System.currentTimeMillis() + GraceQuantumMillis;
	}

	public Vehicle(String plateNumber, String lotNumber) {
		this.plateNumber = plateNumber;
		this.lotNumber = lotNumber;

		this.entryDate = System.currentTimeMillis();

		// Set the grace period end date to be entry date + grace period time
		// amount
		this.graceEndDate = System.currentTimeMillis() + GraceQuantumMillis;

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

	public void setEntryDate(long entryDate){
		this.entryDate = entryDate;
	}
	
	public long getEntryDate() {
		return entryDate;
	}

	public void setGraceEndDate(long graceEndDate){
		this.graceEndDate = graceEndDate;
	}
	
	public long getGraceEndDate() {
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
		if (o.toString().equals(this.toString())) {
			return true;
		}

		return false;
	}
}
