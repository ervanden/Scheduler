package scheduler;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;

public class PiButton {

    // time of last pin state change
    int p_h;
    int p_m;
    int p_s;
    int p_mil;
    // time when last picture was taken
    int pic_h;
    int pic_m;
    int pic_s;
    int pic_mil;

    public PiButton(int pin) {

        final GpioPinDigitalInput myButton = Pi4j.initInputPin(pin);

        myButton.addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                LocalDateTime now = LocalDateTime.now();
                int h = now.getHour();
                int m = now.getMinute();
                int s = now.getSecond();
                int mil = now.getNano() / 1000000;
                int delta = (mil + s * 1000 + m * 60000 + h * 3600000)
                        - (p_mil + p_s * 1000 + p_m * 60000 + p_h * 3600000);
                p_h = h;
                p_m = m;
                p_s = s;
                p_mil = mil;

                System.out.println(delta + " msec  "
                        + "--> GPIO PIN " + pin
                        + " STATE CHANGE: " + event.getPin()
                        + " = " + event.getState());

                int pic_delta = (mil + s * 1000 + m * 60000 + h * 3600000)
                        - (pic_mil + pic_s * 1000 + pic_m * 60000 + pic_h * 3600000);

                
                pic_h = h;
                pic_m = m;
                pic_s = s;
                pic_mil = mil;
                
                System.out.println(pic_delta + " msec  = picture delta");
                
                if (pic_delta > 2000) {
                    
                    // do not take pictures faster than one per 2 seconds

                    Process p;
                    try {
                        System.out.println("/home/pi/Scheduler/takePicture");
                        p = Runtime.getRuntime().exec("/home/pi/Scheduler/takePicture");
                        p.waitFor();
                        BufferedReader reader
                                = new BufferedReader(new InputStreamReader(p.getInputStream()));

                        String line = "";
                        while ((line = reader.readLine()) != null) {
                            System.out.println(line + "\n");
                        }

                    } catch (Exception e) {
                        System.out.println("ERROR : could not execute command");
                    }
                }
            }

        });

        System.out.println(" ... Listening on GPIO #02.");

        try {
            while (true) {
                Thread.sleep(5000);
            }
        } catch (InterruptedException ie) {
            System.out.println("PiButton sleep interrupted exception");
        };

        // stop all GPIO activity/threads by shutting down the GPIO controller
        // (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
        // gpio.shutdown();   <--- implement this method call if you wish to terminate the Pi4J GPIO controller
    }
}
