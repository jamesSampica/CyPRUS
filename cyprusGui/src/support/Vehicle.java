/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package support;

/**
 *
 * @author James
 */
public class Vehicle {

    private String plateNumber;
    private String lotNumber;
    private Integer timeLeft;
    private String date;

    public Vehicle(String plateNumber, String lotNumber, String date) {
        timeLeft = 30;
        this.plateNumber = plateNumber;
        this.lotNumber = lotNumber;
        this.date = date;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
    
    public Integer getTimeLeft(){
        return timeLeft;
    }
    
    public void setTimeLeft(int timeLeft){
        this.timeLeft = timeLeft;
    }

    @Override
    public String toString() {
        return plateNumber + " " + lotNumber + " " + timeLeft.toString() + " " + date;
    }
}
