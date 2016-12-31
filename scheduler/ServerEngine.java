package scheduler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
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
        // control GPIO based on the schedule
        restoreSchedule();  // from scheduleFileName
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
            if (Scheduler.windows) {
                scheduleFileName = "C:\\Users\\erikv\\Documents\\Scheduler.txt";
            }
            System.out.println(scheduleFileName);
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
            if (Scheduler.windows) {
                scheduleFileName = "C:\\Users\\erikv\\Documents\\Scheduler.txt";
            }
            System.out.println(scheduleFileName);
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
    
    public TimeValue previousEvent(TimeValue t){
        TimeValue tprev=null;
        return tprev;
    }
    
        public TimeValue nextEvent(TimeValue t){
        TimeValue tnext=null;
        return tnext;
    }
}
