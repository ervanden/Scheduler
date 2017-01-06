package scheduler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ServerEngine {

    // control GPIO based on the schedule
    static public boolean STATE = false;
    static public boolean expired = false;

    // expired=true  means that the current time is after the dates in the schedule
    // and the one time events are no longer to be executed.
    static int columnCount = 7;
    static int rowCount = 24 * 4;
    static TimeValue[][] tableData = new TimeValue[rowCount][columnCount];

    static String scheduleFileName = "/home/pi/Scheduler/Schedule.txt";

    static ArrayList<String> weekdays = new ArrayList<>();
    static ServerEngineThread serverEngineThread = new ServerEngineThread();

    {
        weekdays.add("MONDAY");  // will all be overwritten when schedule is restored
        weekdays.add("TUESDAY");
        weekdays.add("WEDNESDAY");
        weekdays.add("THURSDAY");
        weekdays.add("FRIDAY");
        weekdays.add("SATURDAY");
        weekdays.add("SUNDAY");

        for (int col = 0; col < columnCount; col++) {
            for (int row = 0; row < rowCount; row++) {
                tableData[row][col] = null;
            }
        }
    }

    public ServerEngine() {
        
        Pi4j.initPin(Scheduler.server_pin);
        
          ServerEngine.STATE=Pi4j.readPin();// dummy for now
          
        /*
        for (int i =1; i<10; i++){
         Pi4j.switchOn();
         try{ Thread.sleep(1000);} catch (Exception e){};
         Pi4j.switchOff();
         try{ Thread.sleep(1000);} catch (Exception e){};
         }
         */
    }

    static public boolean scheduleHasData() {
        return tableData[0][0] != null;

    }

    static public void start() {
        serverEngineThread.start();
    }

    static public ArrayList<String> restart() {
        Scheduler.serverMessage(1, "serverEngine is asked to restart");
        serverEngineThread.restart();
        ArrayList<String> reply = new ArrayList<>();
        reply.add("ok");
        return reply;
    }

    static public int dayToColumn(String day) {
        return weekdays.indexOf(day);
    }

    static public ArrayList<String> newSchedule(ArrayList<String> timeValueList) {
        Scheduler.serverMessage(1, "Receiving schedule update");
        int col = 0;
        int row = 0;
        for (String line : timeValueList) {
            TimeValue timeValue = TimeValue.stringToTimeValue(line);
            weekdays.set(col, timeValue.dayName());
            tableData[row][col] = timeValue;
            row = (row + 1) % rowCount;
            if (row == 0) {
                col++;
            }
        }

        ArrayList<String> reply = new ArrayList<>();
        reply.add("ok");
        return reply;
    }

    static public ArrayList<String> getSchedule(ArrayList<String> day) {
        ArrayList<String> reply = new ArrayList<>();

        Scheduler.serverMessage(1, "Sending schedule for " + day.get(0));
        int col = dayToColumn(day.get(0));

        // if tableData has no values (first start of pi) return an empty list
        if (tableData[0][col] == null) {
            Scheduler.serverMessage(1, "No data to send");
            return reply;
        } else {
            for (int row = 0; row < rowCount; row++) {
                reply.add(tableData[row][col].asString());
            }
        }
        return reply;
    }

    static public ArrayList<String> saveSchedule() {
        ArrayList<String> reply = new ArrayList<>();

        // Stores the schedule in a file to restore after pi boot
        // This procedure is called after every update of the schedule
        Scheduler.serverMessage(1, "Saving the schedule to " + scheduleFileName);
        try {
            File initialFile = new File(scheduleFileName);
            OutputStream is = new FileOutputStream(initialFile);
            OutputStreamWriter isr = new OutputStreamWriter(is, "UTF-8");
            BufferedWriter outputStream = new BufferedWriter(isr);

            // tableData[][] is always populated since saveSchedule() is called after an 
            // update from the client
            for (String day : weekdays) {
                int col = dayToColumn(day);
                for (int row = 0; row < rowCount; row++) {
                    outputStream.write(tableData[row][col].asString());
                    outputStream.newLine();
                }
            }
            outputStream.close();
            reply.add("ok");

        } catch (IOException io) {
            System.err.println("io exception while writing to " + scheduleFileName);
            reply.add("io exception while writing to " + scheduleFileName);
        }
        return reply;
    }

    static public void restoreSchedule() {  // called when ServerEngine starts
        ArrayList<String> msg;
        BufferedReader inputStream = null;

        try {
            File initialFile = new File(scheduleFileName);
            InputStream is = new FileInputStream(initialFile);
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            inputStream = new BufferedReader(isr);

            msg = new ArrayList<>();
            for (int col = 0; col < columnCount; col++) {
                for (int row = 0; row < rowCount; row++) {
                    msg.add(inputStream.readLine());
                }
            }
            ServerEngine.newSchedule(msg);

            inputStream.close();

        } catch (IOException io) {
            System.err.println(" io exception while reading from " + scheduleFileName);
        }
    }

    static public TimeValue previousEvent(String dayName, int hour, int minute) {
        int row = hour * 4 + minute / 15;
        int col = dayToColumn(dayName);
        return tableData[row][col];
    }

    static public TimeValue nextEvent(String dayName, int hour, int minute) {
        int row = hour * 4 + minute / 15;
        int col = dayToColumn(dayName);
        row = (row + 1) % rowCount;
        if (row == 0) { // overflow to next day
            col = (col + 1) % 7;
        }
        return tableData[row][col];
    }

    static public void expireOnDate(TimeValue tnow) {

        if (!expired) {
            if (!tnow.isSameDateAs(tableData[0][dayToColumn(tnow.dayName())])) {
                expired = true;
                Scheduler.serverMessage(1, "expire on date ");
            }
        }
    }

    static public void expireOnEndOfSchedule(TimeValue tnow) {

        if (!expired) {
            // check if tnow is in the last time slot of the schedule
            int row = tnow.hour() * 4 + tnow.minute() / 15;
            int col = dayToColumn(tnow.dayName());

            if ((row == (rowCount - 1)) && (col == (columnCount - 1))) {
                expired = true;
                Scheduler.serverMessage(1, "expire on end of schedule ");
            }
        }
    }
}
