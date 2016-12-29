package scheduler;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class piClient {

    String piAddress = "192.168.0.2";
    int piPort = 6789;

    public void sendSchedule() {

    }

    public ArrayList<String> send(ArrayList<String> msg) {

        // send msg txt to server and return reply txt from server
        ArrayList<String> reply = new ArrayList<>();

        try {
            Socket clientSocket = new Socket();

//            clientSocket.connect(new InetSocketAddress("192.168.0.2", 6789), 3000);
            clientSocket.connect(new InetSocketAddress("localhost", 6789), 3000);

            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            for (String line : msg) {
                outToServer.writeBytes(line + "\n");
            }
            outToServer.writeBytes(".\n");

            String line = inFromServer.readLine();
            while (!line.equals(".")) {
                reply.add(line);
                line = inFromServer.readLine();
            }

//            clientSocket.close();
            return reply;
            
        } catch (Exception se) {
            System.out.println("ping - Pi does not respond");
            System.out.println(se.getMessage());
            reply.add(se.getMessage());
            return reply;
        }

    }

}
