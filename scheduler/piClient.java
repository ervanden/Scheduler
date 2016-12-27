package scheduler;

import java.io.*;
import java.net.*;

public class piClient {

    String piAddress = "192.168.0.2";
    int piPort = 6789;

    public String ping(String msg) {
        try {
            String reply;
            Socket clientSocket = new Socket();

            clientSocket.connect(new InetSocketAddress("192.168.0.2", 6789), 3000);

            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            outToServer.writeBytes(msg + "\n");
            reply = inFromServer.readLine();
            System.out.println(reply);
            clientSocket.close();
            return reply;
        } catch (Exception se) {
            System.out.println("ping - Pi does not respond");
            System.out.println(se.getMessage());
            return se.getMessage();
        }

    }

}
