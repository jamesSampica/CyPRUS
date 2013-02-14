/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package renderers;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 *
 * @author James
 */
public class PlateDataListRenderer extends JLabel implements ListCellRenderer<Object> {
     public PlateDataListRenderer() {
     }

     @Override
     public Component getListCellRendererComponent(JList<?> list,
                                                   Object value,
                                                   int index,
                                                   boolean isSelected,
                                                   boolean cellHasFocus) {

         setText(value.toString());

         return this;
     }
 }
