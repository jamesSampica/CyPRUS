/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tableModels;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import support.Vehicle;

/**
 *
 * @author James
 */
public class RecentVehicleDataModel extends AbstractTableModel {
    
    public static int PlateNumberColumn = 0;
    public static int LotNumberColumn = 1;
    public static int DateEnteredColumn = 2;
    
    private List<Vehicle> vehicles;
    private SimpleDateFormat rowDateFormatter;
    
    public RecentVehicleDataModel(){
        vehicles = Collections.synchronizedList(new ArrayList<Vehicle>());
        rowDateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    }
    
    @Override
    public int getRowCount() {
       return vehicles.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        
        if(rowIndex >= vehicles.size()){
            return null;
        }
        
        Vehicle selectedVehicle = vehicles.get(rowIndex);
        if(columnIndex == PlateNumberColumn){
            return selectedVehicle.getPlateNumber();
        }
        else if(columnIndex == LotNumberColumn){
            return selectedVehicle.getLotNumber();
        }
        
        return rowDateFormatter.format(selectedVehicle.getGraceEndDate());
    }
    
     /**
     * Returns a row that represents a vehicle
     * @param rowIndex index of the row to get
     * @return the vehicle at the specified rowindex
     */
    public Vehicle getRow(int rowIndex){
        return vehicles.get(rowIndex);
    }
    
     /**
     * Adds a row to the table
     * @param vehicle the vehicle to add
     */   
    public void addRow(Vehicle vehicle) {

        //This list has a cap of 50
        if (vehicles.size() >= 50) {
            removeRow(vehicles.get(vehicles.size() - 1));
        }
        vehicles.add(0, vehicle);
        int row = vehicles.indexOf(vehicle);
        fireTableRowsInserted(row, row);
    }
    
     /**
     * Removes a row from the table
     * @param vehicle the vehicle to remove
     */ 
    public void removeRow(Vehicle vehicle) {
        int row = vehicles.indexOf(vehicle);
        vehicles.remove(vehicle);
        fireTableRowsDeleted(row, row);
    }
    
     /**
     * Clears the table data
     */
    public void clearData(){
        vehicles.clear();
        this.fireTableDataChanged();
    }
}
