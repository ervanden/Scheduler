package scheduler;

public class ServerEngineThread extends Thread {

    private boolean stop = false;

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

    private void startScheduling() {
        System.out.println("Restart scheduling");

        stop = false;

        while (true) {
            try {
                System.out.println("sleeping...");
                Thread.sleep(3000);
            } catch (InterruptedException ie) {

            }
            if (stop) {
                System.out.println("startScheduling asked to stop");
                return;
            }
        }
    }
}
