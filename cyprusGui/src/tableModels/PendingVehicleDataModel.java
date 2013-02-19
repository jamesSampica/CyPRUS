/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tableModels;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.table.AbstractTableModel;
import support.Vehicle;

/**
 *
 * @author James
 */
public class PendingVehicleDataModel extends AbstractTableModel {

    public static int PlateNumberColumn = 0;
    public static int LotNumberColumn = 1;
    public static int TimeRemainingColumn = 2;
    public static int DateEnteredColumn = 3;
    private volatile ArrayList<Vehicle> vehicles;
    private RecentVehicleDataModel recentDataModel;

    public PendingVehicleDataModel(RecentVehicleDataModel recentDataModel) {
        vehicles = new ArrayList();
        this.recentDataModel = recentDataModel;
    }

    @Override
    public int getRowCount() {
        return vehicles.size();
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        if (rowIndex >= vehicles.size()) {
            return null;
        }

        Vehicle selectedVehicle = vehicles.get(rowIndex);
        if (columnIndex == PlateNumberColumn) {
            return selectedVehicle.getPlateNumber();
        } else if (columnIndex == LotNumberColumn) {
            return selectedVehicle.getLotNumber();
        } else if (columnIndex == TimeRemainingColumn) {
            fireTableDataChanged();
            if (selectedVehicle.getGraceEndDate() - System.currentTimeMillis() <= 0) {
                //if (vehicles.contains(selectedVehicle)) {
                    //removeRow(selectedVehicle);
                //}
                //return 0;
            }
            return selectedVehicle.getGraceEndDate() - System.currentTimeMillis();
        }

        return DateFormat.getDateInstance().format(new Date(selectedVehicle.getEntryDate()));
    }

    public Vehicle getRow(int rowIndex) {
        return vehicles.get(rowIndex);
    }

    
    public void addRow(Vehicle vehicle) {
        vehicles.add(vehicle);
        int row = vehicles.indexOf(vehicle);
        for (int column = 0; column < 4; column++) {
            fireTableCellUpdated(row, column);
        }
        fireTableRowsInserted(row, row);
    }

    public void removeRow(Vehicle vehicle) {
        int row = vehicles.indexOf(vehicle);
        vehicles.remove(vehicle);
        recentDataModel.addRow(vehicle);
        for (int column = 0; column < 4; column++) {
            fireTableCellUpdated(row, column);
        }
        this.fireTableRowsDeleted(row, row);
    }
    
    public void removeRowByPlateAndLot(String plateNumber, String lotNumber){   
        for(Vehicle v: vehicles){
            if(v.getPlateNumber().equals(plateNumber) && v.getLotNumber().equals(lotNumber)){
                removeRow(v);
            }
        }
    }
    
    public boolean contains(Vehicle vehicle){
        if(vehicles.contains(vehicle)){
            return true;
        }
        
        return false;
    }
}
