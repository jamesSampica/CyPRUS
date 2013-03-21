/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package renderers;

import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * A basic implementation of cellrenderer to allow JTable headers to have icons
 * @author James
 */
public class JTableHeaderIconRenderer extends DefaultTableCellRenderer{
    @Override
    public Component getTableCellRendererComponent(JTable table,
            Object obj, boolean isSelected, boolean hasFocus, int row,
            int column) {
        return (JComponent) obj;
    }
}
