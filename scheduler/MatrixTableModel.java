package scheduler;

import java.util.ArrayList;
import java.util.Calendar;
import javax.swing.table.DefaultTableModel;

public class MatrixTableModel extends DefaultTableModel {

    int columnCount = 7;
    int rowCount = 24 * 4;
    TimeValue[][] tableData = new TimeValue[rowCount][columnCount];

    public MatrixTableModel() {
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
                    slot.set(Calendar.SECOND, 0);
                    slot.on = false;
                    slot.once = false;
                    tableData[row][col] = slot;
                    row++;
                }
            }
        }
    }

    public void getScheduleFromServer() {
        // If the reply is an empty list , this means that pi has no schedule yet
        for (int col = 0; col < columnCount; col++) {

            ArrayList<String> msg = new ArrayList<>();
            ArrayList<String> reply;

            String dayName = tableData[0][col].dayName();
            Scheduler.clientMessage(1,"Retrieving schedule from pi for " + dayName);
            msg.add("getSchedule");
            msg.add(dayName);

            reply = PiClient.send(msg);
            if (reply.size() == rowCount) {
                for (int row = 0; row < rowCount; row++) {
                    TimeValue timeValueFromPi = TimeValue.stringToTimeValue(reply.get(row));
                    TimeValue timeValueSchedule = tableData[row][col];
                    if (timeValueSchedule.isSameDateAs(timeValueFromPi)) {
                        timeValueSchedule.on = timeValueFromPi.on;
                        timeValueSchedule.once = timeValueFromPi.once;
                    } else { //expired
                        timeValueSchedule.on = timeValueFromPi.on;
                        if (timeValueFromPi.once) {
                            timeValueSchedule.on = !timeValueFromPi.on;
                        }
                        timeValueSchedule.once = false;
                    }

                }
            }
        }
    }

    public void sendScheduleToServer() {
        ArrayList<String> msg;
        ArrayList<String> reply;

        msg = new ArrayList<>();
        msg.add("newSchedule");
        for (int col = 0; col < columnCount; col++) {
            for (int row = 0; row < rowCount; row++) {
                msg.add(tableData[row][col].asString());
            }
        }
        reply = PiClient.send(msg);
        Scheduler.clientMessage(1,reply.get(0));  // "ok"

        Scheduler.clientMessage(1,"Telling pi to save the schedule ... ");
        msg = new ArrayList<>();
        msg.add("saveSchedule");
        reply = PiClient.send(msg);
        Scheduler.clientMessage(1,reply.get(0));  // "ok"

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
        // cells are not editable
    }

    public Boolean getCyclic(int row, int col) {
        return tableData[row][col].on;
    }

    public void setCyclic(int row, int col, Boolean value) {
        tableData[row][col].on = value;
    }

    public Boolean getOnce(int row, int col) {
        return tableData[row][col].once;
    }

    public void setOnce(int row, int col, Boolean value) {
        tableData[row][col].once = value;
    }
}
