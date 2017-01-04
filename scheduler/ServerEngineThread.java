package scheduler;

public class ServerEngineThread extends Thread {

    private boolean stop = false;
    private boolean fastforward = false;

    public ServerEngineThread() {
        super("ServerEngineThread");
    }

    public void run() {
        if (true) {
            while (true) {
                stop = false;
                startScheduling();
            }
        }
    }

    public void restart() {
        Scheduler.serverMessage(1, "serverEngineThread is asked to restart");
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
                Scheduler.serverMessage(1, "stoppableSleep is asked to stop");
                return;
            }
        }
    }

    private void setControl(boolean state, TimeValue tnow) {
        if (ServerEngine.STATE != state) {
            ServerEngine.STATE = state;

        }
    }

    private String printState(TimeValue tnow) {
        String s
                = tnow.dayName()
                + " " + tnow.day() + "/" + tnow.month()
                + " " + tnow.hour() + ":" + tnow.minute()
                + "  STATE=";
        if (ServerEngine.STATE) {
            s = s + "ON";
        } else {
            s = s + "OFF";
        }
        return s;
    }

    private void startScheduling() {
        Scheduler.serverMessage(1, "\nRestart scheduling\n");

        TimeValue tnow;
        TimeValue tprev;
        TimeValue tnext;

        boolean state, currentState, nextState;

        if (!ServerEngine.scheduleHasData()) {
            Scheduler.serverMessage(1, "Schedule has no data. Waiting...");
            stoppableSleep(60);
        } else {
            /* PSEUDO CODE,DO NOT REMOVE    
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

            Scheduler.serverMessage(1, printState(tnow) + "  <----------- Pin STATE");

            while (!stop) {

                /* if fastforward, sleeps are replaced by increasing the time of tnow
                 and tnow is not syncronized with the real time after every loop
                 */
                if (fastforward) {  // not too fast!
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ie) {
                    }
                }

                if (!fastforward) {
                    tnow = new TimeValue();   // synchronize
                }

                // expire if we are one week later than the same day in the schedule
                ServerEngine.expireOnDate(tnow);

                tprev = ServerEngine.previousEvent(tnow.dayName(), tnow.hour(), tnow.minute());
                tnext = ServerEngine.nextEvent(tnow.dayName(), tnow.hour(), tnow.minute());

                Scheduler.serverMessage(2, tprev.dateName() + " < " + tnow.dateName() + "  < " + tnext.dateName());

                int secondsToNextEvent;
                secondsToNextEvent = (tnext.hour() * 60 + tnext.minute()) - (tnow.hour() * 60 + tnow.minute());
                secondsToNextEvent = secondsToNextEvent * 60;

                if (secondsToNextEvent < 0) {
                    // we are in the last timeslot of the day, tnext is next day
                    secondsToNextEvent = 24 * 3600 - (tnow.hour() * 3600 + tnow.minute() * 60);
                }

                Scheduler.serverMessage(2, "seconds to next event = " + secondsToNextEvent);

                /* tprev is always on the same day as tnow */
                currentState = getState(tprev);
                Scheduler.serverMessage(2, "current state according to schedule (tprev)  = " + currentState);

                if (stop) {
                    return;
                }

                if (ServerEngine.STATE != currentState) {
                    if (currentState) {
                        Pi4j.switchOn();
                    } else {
                        Pi4j.switchOff();
                    }
                    Scheduler.serverMessage(1, printState(tnow) + "  <-----------");
                } else {
                    Scheduler.serverMessage(1, printState(tnow));
                }

                if (stop) {
                    return;
                }
                ServerEngine.expireOnEndOfSchedule(tnow);
                // getState will from now on only be called for future events.
                // If tnow is in the last timeslot of the schedule, all future events expire

                nextState = getState(tnext);
                Scheduler.serverMessage(2, "next state according to schedule (tnext) = " + nextState);
                Scheduler.serverMessage(2, "Sleeping " + secondsToNextEvent);

                if (!fastforward) {
                    stoppableSleep(secondsToNextEvent);
                }
                // roll time forward instead of creating a new tnow
                tnow.add(TimeValue.SECOND, secondsToNextEvent);

                if (stop) {
                    return;
                }

                if (ServerEngine.STATE != currentState) {
                    if (currentState) {
                        Pi4j.switchOn();
                    } else {
                        Pi4j.switchOff();
                    }
                    Scheduler.serverMessage(1, printState(tnow) + "  <-----------");
                } else {
                    Scheduler.serverMessage(1, printState(tnow));
                }

                Scheduler.serverMessage(2, "Sleeping " + 5 * 60);

                if (!fastforward) {
                    stoppableSleep(5 * 60);
                }
                tnow.add(TimeValue.SECOND, 5 * 60);

            }
        }

    }

    private boolean getState(TimeValue tschedule) {
        // get the state of the event in tschedule on the date of today.
        // it is assumed that tschedule and today are the same weekday.

        if (ServerEngine.expired) {
            if (tschedule.once) {
                return !tschedule.on;
            } else {
                return tschedule.on;
            }
        } else { // not expired
            return tschedule.on;
        }
    }

}
