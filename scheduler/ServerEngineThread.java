package scheduler;

public class ServerEngineThread extends Thread {

    private boolean debug = false;
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
                //               System.out.println("startScheduling is asked to stop");
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
                            p.dayName() + " " + p.hour() + ":" + p.minute()
                            + " < " + tnow.dayName() + " " + tnow.hour() + ":" + tnow.minute() + "  < "
                            + n.dayName() + " " + n.hour() + ":" + n.minute()
                    );
                }
                int secondsToNextEvent = tnext.secondsLaterThan(tnow);
                if (debug) {
                    System.out.println("seconds to next event = " + secondsToNextEvent);
                }

                /* tprev is always on the same day as tnow */
                currentState = getState(tprev);
                if (debug) {
                    System.out.println("current state according to schedule = " + currentState);
                }

                state = ServerEngine.STATE;
                setControl(currentState, tnow);
                if (ServerEngine.STATE != state) {
                    printState(tnow);
                    System.out.println("  <-----------");
                }

                ServerEngine.expireOnEndOfSchedule(tnow);
                // getState will from now on only be called for events after tnow.
                // So if tnow is in the last timeslot of the schedule, from now on
                // all one-time events have expired

                nextState = getState(tnext);
                if (debug) {
                    System.out.println("next state according to schedule = " + nextState);
                }

                if (debug) {
                    System.out.println("Sleeping " + secondsToNextEvent);
                }

                // roll time forward            
                if (fastforward) {
                    tnow.add(TimeValue.SECOND, secondsToNextEvent);
                } else {
                    stoppableSleep(secondsToNextEvent);
                }

                state = ServerEngine.STATE;
                setControl(nextState, tnow);
                printState(tnow);
                if (ServerEngine.STATE != state) {
                    System.out.println("  <-----------");
                } else {
                    System.out.println();
                };

                if (debug) {
                    System.out.println("Sleeping " + 5 * 60);
                }

                if (fastforward) {
                    tnow.add(TimeValue.SECOND, 5 * 60);
                } else {
                    stoppableSleep(5 * 60);
                }

                System.out.println();

            }
        }

    }
    /*
     private boolean getState(TimeValue tschedule, TimeValue today) {
     // get the state of the event in tschedule on the date of today.
     // it is assumed that tschedule and today are the same weekday.
     if (debug) {
     System.out.println("   getState schedule =  " + tschedule.timeValueName());
     System.out.println("   getState now      =  " + today.timeValueName());
     System.out.println("   isSameDate?      =  " + tschedule.isSameDateAs(today));
     }

     if (tschedule.once) {
     if (tschedule.isSameDateAs(today)) {
     return tschedule.on;
     } else {
     return !tschedule.on;
     }
     } else {
     return tschedule.on;
     }
     }
     */

    private boolean getState(TimeValue tschedule) {
        // get the state of the event in tschedule on the date of today.
        // it is assumed that tschedule and today are the same weekday.
        if (debug) {
            System.out.println("   getState schedule =  " + tschedule.timeValueName());
        }

        if (ServerEngine.expired) {
            if (tschedule.once) {
                return tschedule.on;
            } else {
                return !tschedule.on;
            }
        } else { // not expired
            return tschedule.on;
        }
    }

}
