package scheduler;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

class ServerThread extends Thread {

    private Socket socket = null;

    public ServerThread(Socket socket) {
        super("piServer Thread");
        this.socket = socket;
    }

    public void run() {

        try (
                BufferedReader inFromClient
                = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(socket.getOutputStream());) {

            // wait for client to send a command, followed by text, terminated with "."
            String command;
            ArrayList<String> text = new ArrayList<>();
            ArrayList<String> reply = null;

            command = inFromClient.readLine();

            String line = inFromClient.readLine();
            while (!line.equals(".")) {
                text.add(line);
                line = inFromClient.readLine();
            }

            if (command.equals("newSchedule")) {
                reply = Scheduler.serverEngine.newSchedule(text);
            } else if (command.equals("getSchedule")) {
                reply = Scheduler.serverEngine.getSchedule(text);
            } else if (command.equals("saveSchedule")) {
                reply = Scheduler.serverEngine.saveSchedule();
                            } else if (command.equals("restartScheduler")) {
                reply = Scheduler.serverEngine.restart();
            } else {
                System.err.println("unknown command from client : <" + command + ">");
            }

            for (String l : reply) {
                outToClient.writeBytes(l + "\n");
            }
            outToClient.writeBytes(".\n");
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

public class PiServer {

    public void runServer() { // throws Exception {

        System.out.println("Starting piServer...");
        int portNumber = 6789;
        boolean listening = true;

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("Listening...");
            while (listening) {
                new ServerThread(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
            System.exit(-1);
        }
    }
}
