/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tableModels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.Timer;
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
    private Timer gracePeriodTimer; 
    private SimpleDateFormat rowDateFormatter;

    public PendingVehicleDataModel(RecentVehicleDataModel recentModel) {
        vehicles = new ArrayList();
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

    public Vehicle getRow(int rowIndex) {
        return vehicles.get(rowIndex);
    }

    
    public void addRow(Vehicle vehicle) {
        vehicles.add(vehicle);
        int row = vehicles.indexOf(vehicle);
        fireTableRowsInserted(row, row);
    }

    public void removeRow(Vehicle vehicle) {
        int row = vehicles.indexOf(vehicle);
        vehicles.remove(vehicle);
        recentDataModel.addRow(vehicle);
        fireTableRowsDeleted(row, row);
    }
    
    public boolean contains(Vehicle vehicle){
        if(vehicles.contains(vehicle)){
            return true;
        }
        
        return false;
    }
    
    public void clearData(){
        vehicles.clear();
        this.fireTableDataChanged();
    }
}
