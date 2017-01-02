package scheduler;

import java.util.Calendar;

public class ServerEngineThread extends Thread {

    private boolean stop = false;
    private boolean fastforward = true;

    public ServerEngineThread() {
        super("ServerEngineThread");
    }

    public void run() {
        while (true) {
            stop = false;
            startScheduling();
        }
    }

    public void restart() {
        System.out.println("serverEngineThread is asked to restart");
        stop = true;
        // startScheduling() will now terminate and will be called again in run()
    }

    private void stoppableSleep(int seconds) {

        for (int s = 1; s <= seconds; s++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {

            }
            if (stop) {
                System.out.println("startScheduling is asked to stop");
                return;
            }
        }
    }

    private void setControl(boolean state, TimeValue tnow) {
        if (ServerEngine.STATE != state) {
            System.out.println();
            System.out.print(
                    tnow.dayName()
                    + " " + tnow.dayName() + "/" + tnow.month()
                    + " " + tnow.hour() + ":" + tnow.minute()
                    + "  SWITCH ");
            if (state) {
                System.out.println("ON");
            } else {
                System.out.println("OFF");
            }
            System.out.println();
            ServerEngine.STATE = state;

        }
    }

    private void startScheduling() {
        System.out.println("\nRestart scheduling\n");

        TimeValue tnow;
        TimeValue tprev;
        TimeValue tnext;
        boolean currentState, nextState;

        if (!ServerEngine.scheduleHasData()) {
            System.out.println("Schedule has no data. Waiting...");
            stoppableSleep(60);
        } else {
            /*      
         while (true)
         {
         t=now;
         STATUS := tprev.on
         sleep(tnext-t)
         STATUS := tnext.on
         sleep(5 min)
         }
             */

            tnow = new TimeValue(); //compiler needs initialization

            while (true) {

                if (fastforward) {
    //                stoppableSleep(1);  // not too fast
                }
                if (!fastforward) {
                    tnow = new TimeValue();   // synchronize
                }
                
                // The weekday in the schedule can be today, or N*7 days ago
                // If it is not today, the one-time events in the entire schedule can be invalidated
                
                if (! ServerEngine.cyclicMode){
                    ServerEngine.checkCyclicMode(tnow);
                }

                tprev = ServerEngine.previousEvent(tnow.dayName(), tnow.hour(), tnow.minute());
                tnext = ServerEngine.nextEvent(tnow.dayName(), tnow.hour(), tnow.minute());

                TimeValue p = tprev;
                TimeValue n = tnext;
                System.out.println(
                        p.dayName() + " " + p.hour() + ":" + p.minute()
                        + " < " + tnow.dayName() + " " + tnow.hour() + ":" + tnow.minute() + "  < "
                        + n.dayName() + " " + n.hour() + ":" + n.minute()
                );

                int secondsToNextEvent = tnext.secondsLaterThan(tnow);
                System.out.println("seconds to next event = " + secondsToNextEvent);

                currentState = getState(tprev, tnow);
                System.out.println("current state according to schedule = " + currentState);

                long milliSeconds = 0;
                if (!tnext.dayName().equals(tnow.dayName())) {
                    // the next event is tomorrow, so we have to compare the date of tomorrow 
                    // with the date of the event in the schedule to see if 'once' applies.
                    milliSeconds = tnow.getTimeInMillis();
                    tnow.add(TimeValue.DATE, 1);
                }
                nextState = getState(tnext, tnow);
                System.out.println("next state according to schedule = " + nextState);

                if (milliSeconds > 0) { // means that tnow was set to tomorrow. Roll back to today
                    tnow.setTimeInMillis(milliSeconds);
                }

                setControl(currentState, tnow);

                System.out.println("Sleeping " + secondsToNextEvent);

                // roll time forward            
                if (fastforward) {
                    tnow.add(TimeValue.SECOND, secondsToNextEvent);
                } else {
                    stoppableSleep(secondsToNextEvent);
                }

                setControl(nextState, tnow);

                System.out.println("Sleeping " + 5 * 60);

                if (fastforward) {
                    tnow.add(TimeValue.SECOND, 5 * 60);
                } else {
                    stoppableSleep(5 * 60);
                }

            }
        }

    }

    private boolean getState(TimeValue tschedule, TimeValue today) {
        // get the state of the event in tschedule on the date of today.
        // it is assumed that tschedule and today are the same weekday.
        System.out.println("   getState schedule =  " + tschedule.timeValueName());
        System.out.println("   getState now      =  " + today.timeValueName());
        System.out.println("   isSameDate?      =  " + tschedule.isSameDateAs(today));
        if (tschedule.once) {

            if (tschedule.isSameDateAs(today)) {
                return tschedule.cyclic;
            } else {
                return !tschedule.cyclic;
            }
        } else {
            return tschedule.cyclic;
        }
    }
}
