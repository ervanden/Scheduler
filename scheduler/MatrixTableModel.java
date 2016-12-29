package scheduler;

import java.util.ArrayList;
import java.util.Calendar;
import javax.swing.table.DefaultTableModel;

public class MatrixTableModel extends DefaultTableModel {

    int columnCount = 7;
    int rowCount = 24 * 4;
    TimeValue[][] tableData = new TimeValue[rowCount][columnCount];

    public MatrixTableModel(piClient client) {
        super();

        for (int col = 0; col < columnCount; col++) {
            TimeValue day = new TimeValue();
            day.add(Calendar.DAY_OF_MONTH, col);
            int row = 0;
            for (int hr = 0; hr <= 23; hr++) {
                for (int min = 0; min <= 45; min = min + 15) {
                    TimeValue slot = new TimeValue(day);
                    slot.set(Calendar.HOUR_OF_DAY, hr);
                    slot.set(Calendar.MINUTE, min);
                    slot.cyclic = false;
                    slot.once = false;
                    tableData[row][col] = slot;
                    row++;
                }
            }
        }

        // load the values from the server
    }

    public void sendScheduleToServer(piClient client) {
        ArrayList<String> daySchedule;
        ArrayList<String> reply;


        for (int col = 0; col < columnCount; col++) {
            daySchedule = new ArrayList<>();
                    daySchedule.add("newSchedule");
            for (int row = 0; row < rowCount; row++) {
                daySchedule.add(tableData[row][col].asString());
            }
            reply = client.send(daySchedule);
        }

    }

    public int getColumnCount() {
        return columnCount;
    }

    public int getRowCount() {
        return rowCount;
    }

    public String getColumnName(int col) {
        if (col == 0) {
            return "TODAY";
        } else {
            TimeValue t = tableData[0][col];
            return t.dayShortName() + " " + t.day() + "/" + t.month();
        }
    }

    public Object getValueAt(int row, int col) {
        String h = tableData[row][col].hour().toString();
        String m = tableData[row][col].minute().toString();
        if (m.equals("0")) {
            m = "00";
        }
        return h + ":" + m;
    }

    public Class getColumnClass(int c) {
        String s = "";
        return s.getClass();
    }

    public boolean isCellEditable(int row, int col) {
        return false;
    }

    public void setValueAt(Object value, int row, int col) {
        // fireTableDataChanged();
    }

    public void toggle(int row, int col) {
        tableData[row][col].cyclic = !tableData[row][col].cyclic;
    }

    public Boolean getCyclic(int row, int col) {
        return tableData[row][col].cyclic;
    }

    public void setCyclic(int row, int col, Boolean value) {
        tableData[row][col].cyclic = value;
    }

    public Boolean getOnce(int row, int col) {
        return tableData[row][col].once;
    }

    public void setOnce(int row, int col, Boolean value) {
        tableData[row][col].once = value;
    }
}
