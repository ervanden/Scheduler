package scheduler;

import java.util.ArrayList;
import java.util.Calendar;

public class ServerEngine {

    int columnCount = 7;
    int rowCount = 24 * 4;
    TimeValue[][] tableData = new TimeValue[rowCount][columnCount];

    public ServerEngine() {
        // control GPIO based on the schedule

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

    }

    static public int dayToColumn(String day) {
        int c=100;
        if (day.equals("MONDAY")) {
            c = 0;
        }
        if (day.equals("TUESDAY")) {
            c = 1;
        }
        if (day.equals("WEDNESDAY")) {
            c = 2;
        }
        if (day.equals("THURSDAY")) {
            c = 3;
        }
        if (day.equals("FRIDAY")) {
            c = 4;
        }
        if (day.equals("SATURDAY")) {
            c = 5;
        }
        if (day.equals("SUNDAY")) {
            c = 6;
        }
        if (c==100)System.err.println("Argument to dayToInt() is not a day : "+day);
        return c;
    }

    public ArrayList<String> newSchedule(ArrayList<String> timeValueList) {
        ArrayList<String> reply = new ArrayList<>();
        int row=0;
        int col;
        for (String line : timeValueList) {
            System.out.println("server engine received : " + line);
            TimeValue timeValue =TimeValue.stringToTimeValue(line);
//            timeValue.print();
            col=ServerEngine.dayToColumn(timeValue.dayName());
            System.out.println("col="+col+" row="+row+" "+timeValue.asString());
            tableData[row][col]=timeValue;
        }
        reply.add("ok");
        return reply;
    }
}
