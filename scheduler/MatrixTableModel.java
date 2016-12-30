package scheduler;

import java.util.ArrayList;
import java.util.Calendar;
import javax.swing.table.DefaultTableModel;

public class MatrixTableModel extends DefaultTableModel {

    int columnCount = 7;
    int rowCount = 24 * 4;
    TimeValue[][] tableData = new TimeValue[rowCount][columnCount];

    public MatrixTableModel(PiClient client) {
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
        // If the reply is an empty list , this means that pi has no schedule yet
        for (int col = 0; col < columnCount; col++) {

            ArrayList<String> msg = new ArrayList<>();
            ArrayList<String> reply;

            String dayName = tableData[0][col].dayName();
            System.out.println("getSchedule " + dayName);
            msg.add("getSchedule");
            msg.add(dayName);

            reply = client.send(msg);
            if (reply.size()==rowCount) {
                for (int row = 0; row < rowCount; row++) {
                    TimeValue timeValueFromPi = TimeValue.stringToTimeValue(reply.get(row));
                    TimeValue timeValueCurrent = tableData[row][col];
                    timeValueCurrent.cyclic = timeValueFromPi.cyclic;
                    if (timeValueCurrent.isSameDateAs(timeValueFromPi)) {
                        timeValueCurrent.once = timeValueFromPi.once;
                    }

                }
            }
        }
    }

    public void sendScheduleToServer(PiClient client, int[] selectedColumns) {
        ArrayList<String> msg;
        ArrayList<String> reply;

        for (int c = 0; c < selectedColumns.length; c++) {  // send only the modified columns
            int col = selectedColumns[c];
            String dayName = tableData[0][col].dayName();
            System.out.print("sending schedule for " + dayName + " ... ");

            msg = new ArrayList<>();
            msg.add("newSchedule");
            for (int row = 0; row < rowCount; row++) {
                msg.add(tableData[row][col].asString());
            }
            reply = client.send(msg);
            System.out.println(reply.get(0));  // "ok"
        }

        System.out.println("Telling pi to save the schedule ... ");
        msg = new ArrayList<>();
        msg.add("saveSchedule");
        reply = client.send(msg);
        System.out.println(reply.get(0));  // "ok"

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
