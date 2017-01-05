package scheduler;

import com.pi4j.io.gpio.*;

public class Pi4j {

    static GpioPinDigitalOutput pin;
    static GpioController gpio;

    static public boolean switchOn() {
        Scheduler.serverMessage(2, "Pi4J Pin On");
        if (Scheduler.server_controlActive) {
            pin.high();
        }
        return true;
    }

    static public boolean switchOff() {
        Scheduler.serverMessage(2, "Pi4J Pin Off");
        if (Scheduler.server_controlActive) {
            pin.high();
        }
        return false;
    }

    static public boolean readPin() {
        return false;
    }

    public static void initialize() {
        if (Scheduler.server_controlActive) {
            gpio = GpioFactory.getInstance();
            pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_06, "LED", PinState.LOW);
        }
    }
}
