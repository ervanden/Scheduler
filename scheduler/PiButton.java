package scheduler;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class PiButton {

    public PiButton(int pin) {
        System.out.println("<--Pi4J--> GPIO Listen Example ... started.");

        final GpioPinDigitalInput myButton = Pi4j.initInputPin(2);

        myButton.addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                // display pin state on console
                System.out.println(" --> GPIO PIN " + pin + " STATE CHANGE: " + event.getPin() + " = " + event.getState());
            }

        });

        System.out.println(" ... complete the GPIO #02 circuit and see the listener feedback here in the console.");

        try {
            while (true) {
                Thread.sleep(500);
            }
        } catch (InterruptedException ie) {
        };

        // stop all GPIO activity/threads by shutting down the GPIO controller
        // (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
        // gpio.shutdown();   <--- implement this method call if you wish to terminate the Pi4J GPIO controller
    }
}
