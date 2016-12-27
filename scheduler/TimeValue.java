package scheduler;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class TimeValue extends GregorianCalendar {
    /*
     YEAR
     MONTH
     DAY_OF_MONTH
     DAY_OF_WEEK
     WEEK_OF_MONTH
     DAY_OF_WEEK_IN_MONTH
     AM_PM
     HOUR, HOUR_OF_DAY, MINUTE
     */

    Object value;

    public TimeValue() { // returns an object corresponding to now
        super();
        LocalDateTime now = LocalDateTime.now();

        System.out.println(now.getYear());
        System.out.println(now.getMonth().getValue()-1);
        System.out.println(now.getDayOfMonth());
        System.out.println("=" + now.getDayOfWeek());
        System.out.println(now.getHour());
        System.out.println(now.getMinute());

        this.set(now.getYear(),
                now.getMonth().getValue()-1,  // Calendar works with months 0-11
                now.getDayOfMonth(),
                now.getHour(),
                now.getMinute());

        value = null;
        /*
         System.out.println(now.getYear());
         System.out.println(now.getMonthValue());
         System.out.println(now.getDayOfMonth());
         System.out.println("=" + now.getDayOfWeek());
         System.out.println(now.getHour());
         System.out.println(now.getMinute());
         */
    }

    public TimeValue(TimeValue t) {
//     TimeValue tnew = new TimeValue();
        super();
        this.set(
                t.get(Calendar.YEAR),
                t.get(Calendar.MONTH),
                t.get(Calendar.DAY_OF_MONTH),
                t.get(Calendar.HOUR_OF_DAY),
                t.get(Calendar.MINUTE)
        );

    }
    
    public int year(){return get(Calendar.YEAR);}
    public int month() {return get(Calendar.MONTH)+1;}
    public int day() {return get(Calendar.DAY_OF_MONTH);}    
    public String dayName() {
        String[] days = {"SUNDAY", "MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY"};
    return days[get(Calendar.DAY_OF_WEEK)-1];}
    public String dayShortName(){
        return dayName().substring(0,3);
    }
    public int hour() {return get(Calendar.HOUR_OF_DAY);} 
    public int minute() {return get(Calendar.MINUTE);}     
    
    
    public void print() {

        System.out.println(dayShortName()+" "+year()+" "+month()+" "+day()+" "+hour()+" "+minute());

    }

    public void incrementDay() {
        add(Calendar.DAY_OF_MONTH, 1);
    }
}
