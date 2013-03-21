/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tableModels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Timer;
import javax.swing.table.AbstractTableModel;
import support.Vehicle;

/**
 * An implementation of the AbstractTableModel to serve for pending vehicles.
 * @author James
 */
public class PendingVehicleDataModel extends AbstractTableModel {

    public static int PlateNumberColumn = 0;
    public static int LotNumberColumn = 1;
    public static int TimeRemainingColumn = 2;
    public static int DateEnteredColumn = 3;
    
    private volatile List<Vehicle> vehicles;
    private RecentVehicleDataModel recentDataModel;
    private Timer gracePeriodTimer; 
    private SimpleDateFormat rowDateFormatter;

    /**
     * Creates a new model to serve for pending vehicles
     * @param recentModel the recent data model to add vehicles to after they expire
     */
    public PendingVehicleDataModel(RecentVehicleDataModel recentModel) {
        vehicles = Collections.synchronizedList(new ArrayList<Vehicle>());
        recentDataModel = recentModel;
        
        ActionListener taskPerformer = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if(vehicles.size() > 0 
                        && vehicles.get(0).getGraceEndDate().getTime() - System.currentTimeMillis() <= 0){
                    recentDataModel.addRow(vehicles.get(0));
                    vehicles.remove(vehicles.get(0));
                    
                }
            }
            };
        gracePeriodTimer = new Timer( 100 , taskPerformer);
        gracePeriodTimer.setRepeats(true);
        gracePeriodTimer.start();

        rowDateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
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
                return selectedVehicle.getGraceEndDate().getTime() - System.currentTimeMillis();
        }

        return rowDateFormatter.format(selectedVehicle.getEntryDate());
    }

    /**
     * Returns a row that represents a vehicle
     * @param rowIndex index of the row to get
     * @return the vehicle at the specified rowindex
     */
    public Vehicle getRow(int rowIndex) {
        return vehicles.get(rowIndex);
    }

    /**
     * Adds a row to the table
     * @param vehicle the vehicle to add
     */
    public void addRow(Vehicle vehicle) {
        vehicles.add(vehicle);
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
        recentDataModel.addRow(vehicle);
        fireTableRowsDeleted(row, row);
    }
    
    /**
     * Determines if the table contains a vehicle
     * @param vehicle the vehicle to test
     * @return true if it contains the specified vehicle, false otherwise
     */
    public boolean contains(Vehicle vehicle){
        if(vehicles.contains(vehicle)){
            return true;
        }
        
        return false;
    }
    
    /**
     * Clears the table data
     */
    public void clearData(){
        vehicles.clear();
        this.fireTableDataChanged();
    }
}
