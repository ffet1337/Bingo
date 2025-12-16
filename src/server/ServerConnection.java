package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerConnection {
    private ServerStatus status;
    private volatile boolean receiving;
    private ConnectionListener listener;
    private int port;
    private ServerSocket ss;
    private List<Socket> connectedClients;
    private List<Integer> clientsIds;

    enum ServerStatus{
        RECEIVING_CONNECTIONS,
        WAITING_CONFIGURATION,
        STARTING_SERVER,
        ERROR
    }

    ServerConnection() throws IOException {
        receiving = false;
        status = ServerStatus.WAITING_CONFIGURATION;
        connectedClients = new CopyOnWriteArrayList<>();
    }

    void startReceiving(int port) throws IOException {

        ss = new ServerSocket(port);
        this.port = port;
        receiving = true;
        status = ServerStatus.STARTING_SERVER;

        Thread t = new Thread(this::receivingConnections);
        t.start();
    }

    void sendDataArrayTo(int connectionIndex, List<Integer> data) throws IOException {
        Socket s = connectedClients.get(connectionIndex);

        DataOutputStream out = new DataOutputStream(s.getOutputStream());

        out.writeInt(data.size());

        for (int value : data) {
            out.writeInt(value);
        }
        out.flush();
    }

    int getConnectionsCount(){
        return connectedClients.size();
    }

    void receivingConnections() {
        try{
            Socket s;
            while (receiving){
                status = ServerStatus.RECEIVING_CONNECTIONS;
                s = ss.accept();
                connectedClients.add(s);

                listener.onClientConnected(s);
                startWaitingForMessages(s);
            }
        } catch (IOException e) {
            if(!receiving) {
                status = ServerStatus.WAITING_CONFIGURATION;
            }else{
                System.out.println(e.getMessage());
                status = ServerStatus.ERROR;
            }
        }
    }

    void startWaitingForMessages(Socket s){
        Runnable rn = new ClientMessageManager(s, listener);
        new Thread(rn).start();
    }

    void removeClient(Socket s) throws IOException {
        for(Socket so : connectedClients){
            if(so.equals(s)){
                so.close();
                connectedClients.remove(so);
            }
        }
    }

    void removeClient(int i) throws IOException {
        Socket s = connectedClients.remove(i);
        s.close();
    }

    void broadcastData(int data) throws IOException {
        for(Socket s : connectedClients){
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            out.writeInt(data);
            out.flush();
        }
    }

    void stopReceiving() throws IOException {
        receiving = false;
        ss.close();
        status = ServerStatus.WAITING_CONFIGURATION;
    }

    ServerStatus getStatus(){
        return status;
    }

    public void setListener(ConnectionListener listener) {
        this.listener = listener;
    }
}
