package scheduler;

public class ServerEngineThread extends Thread {

    private boolean stop = false;
    private boolean fastforward = true;

    public ServerEngineThread() {
        super("ServerEngineThread");
    }

    public void run() {
        while (true) {
            startScheduling();
        }
    }

    public void restart() {
        System.out.println("serverEngineThread is asked to restart");
        stop = true;
        // startScheduling() will now terminate and will be called again in run()
    }

    private void stoppableSleep(int seconds) {

        if (fastforward) {
            return;
        }
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

        }
    }

    private void startScheduling() {
        System.out.println("Restart scheduling");

        TimeValue tnow;
        TimeValue tprev;
        TimeValue tnext;
        boolean currentState, nextState;

        stop = false;
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

        int timeslots = 1;
        while (timeslots <= 10) {
            timeslots++;

            if (!fastforward) {
                tnow = new TimeValue();   // synchronize
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
            System.out.println("second to next event = " + secondsToNextEvent);

            currentState = getState(tprev, tnow);
            System.out.println("current state according to schedule = " + currentState);

            if (!tnext.dayName().equals(tnow.dayName())) {
                // the next event is tomorrow, so we have to compare the date of tomorrow 
                // with the date of the event in the schedule to see if 'once' applies.
                tnow.add(TimeValue.DATE, 1);
            }
            nextState = getState(tnext, tnow);
            tnow.add(TimeValue.DATE, -1);
            System.out.println("next state according to schedule = " + nextState);

            setControl(currentState, tnow);

            stoppableSleep(secondsToNextEvent);
            System.out.println("Sleeping " + secondsToNextEvent);

            // roll time forward            
            if (fastforward) {
                tnow.add(TimeValue.SECOND, secondsToNextEvent);
            }

            setControl(nextState, tnow);

            stoppableSleep(5 * 60);
            System.out.println("Sleeping " + 5 * 60);
            if (fastforward) {
                tnow.add(TimeValue.SECOND, 5 * 60);
            }

        }

    }

    private boolean getState(TimeValue tschedule, TimeValue today) {
        // get the state of the event in tschedule on the date of today.
        // it is assumed that tschedule and today are the same weekday.
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
