package scheduler;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class TimeValue extends GregorianCalendar {
    /* Calendar fields
     YEAR
     MONTH
     DAY_OF_MONTH
     DAY_OF_WEEK
     WEEK_OF_MONTH
     DAY_OF_WEEK_IN_MONTH
     AM_PM
     HOUR, HOUR_OF_DAY, MINUTE
     */

    public Boolean cyclic;
    public Boolean once;

    public TimeValue() { // returns an object corresponding to now
        super();
        LocalDateTime now = LocalDateTime.now();
        this.set(now.getYear(),
                now.getMonth().getValue() - 1, // Calendar works with months 0-11
                now.getDayOfMonth(),
                now.getHour(),
                now.getMinute());
        cyclic = null;
    }

    public TimeValue(TimeValue t) {   // copy constructor
        super();
        this.set(
                t.get(Calendar.YEAR),
                t.get(Calendar.MONTH),
                t.get(Calendar.DAY_OF_MONTH),
                t.get(Calendar.HOUR_OF_DAY),
                t.get(Calendar.MINUTE)
        );

    }

    public Integer year() {
        return get(Calendar.YEAR);
    }

    public Integer month() {
        return get(Calendar.MONTH) + 1;
    }

    public Integer day() {
        return get(Calendar.DAY_OF_MONTH);
    }

    public String dayName() {
        String[] days = {"SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"};
        return days[get(Calendar.DAY_OF_WEEK) - 1];
    }

    public String dayShortName() {
        return dayName().substring(0, 3);
    }

    public Integer hour() {
        return get(Calendar.HOUR_OF_DAY);
    }

    public Integer minute() {
        return get(Calendar.MINUTE);
    }

    public void print() {
        System.out.println(dayShortName() + " " + year() + " " + month() + " " + day() + " " + hour() + " " + minute());
    }

    public String asString() {
        return dayName()
                + " " + year()
                + " " + month()
                + " " + day()
                + " " + hour()
                + " " + minute()
                + " " + cyclic
                + " " + once;

    }

    static public TimeValue stringToTimeValue(String s) {
        String[] tokens = s.split(" ");
        String dayName = tokens[0];
        String year = tokens[1];
        String month = tokens[2];
        String day = tokens[3];
        String hour = tokens[4];
        String minute = tokens[5];
        String cyclic = tokens[6];
        String once = tokens[7];
        TimeValue tv = new TimeValue();
        tv.set(Integer.parseInt(year),
                Integer.parseInt(month) - 1,
                Integer.parseInt(day),
                Integer.parseInt(hour),
                Integer.parseInt(minute));
        tv.cyclic = Boolean.parseBoolean(cyclic);
        tv.once = Boolean.parseBoolean(once);
        System.out.println(">" + Integer.parseInt(year) + " "
                + Integer.parseInt(month) + " "
                + Integer.parseInt(day) + " "
                + Integer.parseInt(hour) + " "
                + Integer.parseInt(minute));
        return tv;

    }
}
