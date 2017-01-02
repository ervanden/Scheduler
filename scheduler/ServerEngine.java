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

    /* expired=true  means that the one time events are no longer to be executed.
    
       The expire(tnow) method checks if the current point in time is the last time slot in the schedule.
       If this is the case, the one-time events in the entire schedule are expired.
    
       It can happen that the pi is temporarily down during the last time slot in the schedule
       and the expiration described above is missed. To catch this, it is always checked if 
       the current day is not later than the corresponding weekday in the
       schedule. In this case also, the one-time events in the entire schedule have expired.
    
    
    
     */
    static int columnCount = 7;
    static int rowCount = 24 * 4;
    static TimeValue[][] tableData = new TimeValue[rowCount][columnCount];

    static String scheduleFileName = "/home/pi/Scheduler/Schedule.txt";

    static ArrayList<String> weekdays = new ArrayList<>();
    static ServerEngineThread serverEngineThread = new ServerEngineThread();

    {
        weekdays.add("MONDAY");
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

        System.out.println("Schedule file name " + scheduleFileName);
        restoreSchedule();  // from scheduleFileName

        /* test previousEvent and nextEvent
        
        for (String day : weekdays) {
            for (int hour = 0; hour < 24; hour++) {
                for (int min = 0; min < 60; min++) {
                    TimeValue p = previousEvent(day, hour, min);
                    TimeValue n = nextEvent(day, hour, min);
                    System.out.println(
                            p.dayName() + " " + p.hour() + ":" + p.minute()
                            + " < " + day + " " + hour + ":" + min + "  < " +
                            n.dayName() + " " + n.hour() + ":" + n.minute()
                    );
                }
            }
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
        System.out.println("serverEngine is asked to restart");
        serverEngineThread.restart();
        ArrayList<String> reply = new ArrayList<>();
        reply.add("ok");
        return reply;
    }

    static public int dayToColumn(String day) {
        return weekdays.indexOf(day);
    }

    static public ArrayList<String> newSchedule(ArrayList<String> timeValueList) {
        System.out.println("Receiving schedule update");
        int row = 0;
        int col = -1;
        for (String line : timeValueList) {
            TimeValue timeValue = TimeValue.stringToTimeValue(line);
            col = ServerEngine.dayToColumn(timeValue.dayName());
            //System.out.println("col=" + col + " row=" + row + " " + timeValue.asString());
            tableData[row][col] = timeValue;
            row++;
        }
        System.out.println("Received " + row + " entries for " + tableData[0][col].dayName());

        ArrayList<String> reply = new ArrayList<>();
        reply.add("ok");
        return reply;
    }

    static public ArrayList<String> getSchedule(ArrayList<String> day) {
        ArrayList<String> reply = new ArrayList<>();

        System.out.println("Sending schedule for " + day.get(0));
        int col = dayToColumn(day.get(0));

        // if tableData has no values (first start of pi) return an empty list
        if (tableData[0][col] == null) {
            System.out.println("No data to send");
            return reply;
        } else {
            for (int row = 0; row < rowCount; row++) {
                reply.add(tableData[row][col].asString());
//              System.out.println("getSchedule sending col=" + col + " row=" + row + " "+ tableData[row][col].asString());
            }
        }
        return reply;
    }

    static public ArrayList<String> saveSchedule() {
        ArrayList<String> reply = new ArrayList<>();

        // Stores the schedule in a file to restore after pi boot
        // This procedure is called after every update of the schedule
        try {
            File initialFile = new File(scheduleFileName);
            OutputStream is = new FileOutputStream(initialFile);
            OutputStreamWriter isr = new OutputStreamWriter(is, "UTF-8");
            BufferedWriter outputStream = new BufferedWriter(isr);
            System.out.println(outputStream);

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
            System.err.println(" io exception while writing to " + scheduleFileName);
            reply.add(" io exception while writing to " + scheduleFileName);
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

            for (int col = 0; col < columnCount; col++) {
                msg = new ArrayList<>();
                for (int row = 0; row < rowCount; row++) {
                    msg.add(inputStream.readLine());
                }
                ServerEngine.newSchedule(msg);
            }

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

    static public void expire(TimeValue tnow) {
        /*
       The expire(tnow) method checks if the current point in time is the last time slot in the schedule.
       If this is the case, the one-time events in the entire schedule are expired.
    
       It can happen that the pi is temporarily down during the last time slot in the schedule
       and the expiration described above is missed. To catch this, it is always checked if 
       the current day is not later than the corresponding weekday in the
       schedule. In this case also, the one-time events in the entire schedule have expired.   
         */
        int row = tnow.hour() * 4 + tnow.minute() / 15;
        int col = dayToColumn(tnow.dayName());
        
        if (!expired) {
            if (!tnow.isSameDateAs(tableData[0][dayToColumn(tnow.dayName())])) {
                expired = true;
                return;
            }
            // check if this is the last event in the schedule as it was sent from the client
            // = last row && next day in the schedule is in the past
            if (row == rowCount-1){
            
            }

        }
    }
}
