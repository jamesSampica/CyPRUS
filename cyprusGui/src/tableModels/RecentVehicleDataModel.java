/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tableModels;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    
    private ArrayList<Vehicle> vehicles;
    private SimpleDateFormat rowDateFormatter;
    
    public RecentVehicleDataModel(){
        vehicles = new ArrayList();
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
    
    public Vehicle getRow(int rowIndex){
        return vehicles.get(rowIndex);
    }
    
    public void addRow(Vehicle vehicle) {

        //This list has a cap of 50
        if (vehicles.size() >= 50) {
            removeRow(vehicles.get(vehicles.size() - 1));
        }
        vehicles.add(0, vehicle);
        int row = vehicles.indexOf(vehicle);
        fireTableRowsInserted(row, row);
    }
    
    public void removeRow(Vehicle vehicle) {
        int row = vehicles.indexOf(vehicle);
        vehicles.remove(vehicle);
        fireTableRowsDeleted(row, row);
    }
    
    public void clearData(){
        vehicles.clear();
        this.fireTableDataChanged();
    }
}
