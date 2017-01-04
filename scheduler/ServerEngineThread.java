package scheduler;

public class ServerEngineThread extends Thread {

    private boolean debug = true;
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
                System.out.println("stoppableSleep is asked to stop");
                return;
            }
        }
    }

    private void setControl(boolean state, TimeValue tnow) {
        if (ServerEngine.STATE != state) {
            ServerEngine.STATE = state;

        }
    }

    private void printState(TimeValue tnow) {
        System.out.print(
                tnow.dayName()
                + " " + tnow.day() + "/" + tnow.month()
                + " " + tnow.hour() + ":" + tnow.minute()
                + "  SWITCH=");
        if (ServerEngine.STATE) {
            System.out.print("ON");
        } else {
            System.out.print("OFF");
        }
    }

    private void startScheduling() {
        System.out.println("\nRestart scheduling\n");

        TimeValue tnow;
        TimeValue tprev;
        TimeValue tnext;

        boolean state, currentState, nextState;

        if (!ServerEngine.scheduleHasData()) {
            System.out.println("Schedule has no data. Waiting...");
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

            printState(tnow);
            System.out.println("  <----------- ServerEngine STATE");

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
                if (debug) {
                    TimeValue p = tprev;
                    TimeValue n = tnext;
                    System.out.println(
                            p.dateName() + " < " + tnow.dateName() + "  < " + n.dateName()
                    );
                }

                int secondsToNextEvent;
                secondsToNextEvent = (tnext.hour() * 60 + tnext.minute()) - (tnow.hour() * 60 + tnow.minute());
                secondsToNextEvent = secondsToNextEvent * 60;

                if (secondsToNextEvent < 0) {
                    // we are in the last timeslot of the day, tnext is next day
                    secondsToNextEvent = 24 * 3600 - (tnow.hour() * 3600 + tnow.minute() * 60);
                }
                if (debug) {
                    System.out.println("seconds to next event = " + secondsToNextEvent);
                }

                /* tprev is always on the same day as tnow */
                currentState = getState(tprev);
                if (debug) {
                    System.out.println("current state according to schedule (tprev)  = " + currentState);
                }

                if (stop) {
                    return;
                }
                state = ServerEngine.STATE;
                setControl(currentState, tnow);
                if (ServerEngine.STATE != state) {
                    printState(tnow);
                    System.out.println("  <-----------");
                }

                if (stop) {
                    return;
                }
                ServerEngine.expireOnEndOfSchedule(tnow);
                // getState will from now on only be called for future events.
                // If tnow is in the last timeslot of the schedule, all future events expire

                nextState = getState(tnext);
                if (debug) {
                    System.out.println("next state according to schedule (tnext) = " + nextState);
                }

                if (debug) {
                    System.out.println("Sleeping " + secondsToNextEvent);
                }

                if (!fastforward) {
                    stoppableSleep(secondsToNextEvent);
                }
                // roll time forward instead of creating a new tnow
                tnow.add(TimeValue.SECOND, secondsToNextEvent);

                if (stop) {
                    return;
                }
                if (true) { // print state on every iteration
                    state = ServerEngine.STATE;
                    setControl(nextState, tnow);
                    printState(tnow);
                    if (ServerEngine.STATE != state) {
                        System.out.println("  <-----------");
                    } else {
                        System.out.println();
                    }
                } else { // print only if state switched
                    state = ServerEngine.STATE;
                    setControl(nextState, tnow);
                    if (ServerEngine.STATE != state) {
                        printState(tnow);
                        System.out.println("  <-----------");
                    }
                }
                if (debug) {
                    System.out.println("Sleeping " + 5 * 60);
                }

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
        if (debug) {
            System.out.println("   getState schedule =  " + tschedule.timeValueName());
        }

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
