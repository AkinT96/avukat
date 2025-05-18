package UI;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class GuthabenZellenRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {

        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        try {
            double guthaben = Double.parseDouble(value.toString());
            if (guthaben < 0) {
                c.setForeground(Color.RED); // Guthaben negativ (Kırmızı)
            } else {
                c.setForeground(new Color(0, 128, 0)); // Guthaben 0 oder positiv (Yeşil)
            }
        } catch (Exception e) {
            c.setForeground(Color.BLACK); // Standardfarbe bei Fehler (Varsayılan renk)
        }

        return c;
    }
}
