package scheduler;

import com.pi4j.io.gpio.*;

public class Pi4j {

    static GpioPinDigitalOutput pin;
    static GpioController gpio;

    static public void switchOn() {
        Scheduler.serverMessage(2, "Pi4J Pin On");
        if (Scheduler.server_controlActive) {
            pin.high();
        }
    }

    static public void switchOff() {
        Scheduler.serverMessage(2, "Pi4J Pin Off");
        if (Scheduler.server_controlActive) {
            pin.high();
        }
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
