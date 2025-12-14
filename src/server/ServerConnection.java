package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerConnection {
    private ServerStatus status;
    private boolean receiving;
    private int port;
    private ServerSocket ss;
    private List<Socket> connectedClients;

    enum ServerStatus{
        RECEIVING_CONNECTIONS,
        WAITING_CONFIGURATION,
        STARTING_SERVER,
        ERROR
    }

    ServerConnection() throws IOException {
        receiving = false;
        status = ServerStatus.WAITING_CONFIGURATION;
        ss = new ServerSocket();
        connectedClients = new ArrayList<>();
    }

    void startReceiving(int port){
        status = ServerStatus.STARTING_SERVER;
        Thread t = new Thread(this::receivingConnections);
        this.port = port;
        receiving = true;
        t.start();
    }

    void receivingConnections() {
        try{
            if(ss.getLocalPort() != this.port) {
                ss = new ServerSocket(port);
            }

            status = ServerStatus.RECEIVING_CONNECTIONS;
            while (receiving){
                Socket s;
                s = ss.accept();
                if(receiving)
                {
                    System.out.println("connected from:  " + s.getPort() + "\n");
                    connectedClients.add(s);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void broadcastData(DataOutputStream data)
    {

    }

    void stopReceiving(){
        receiving = false;
        status = ServerStatus.WAITING_CONFIGURATION;
    }

    ServerStatus getStatus(){
        return status;
    }
}
