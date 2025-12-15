package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientConnection {
    private Socket connection;
    private String serverIp;
    private int serverPort;
    private boolean connected;

    ClientConnection(){
        serverIp = "";
        serverPort = -1;
        connected = false;
    }

    void connect(String ip, int port) throws IOException {
        connection = new Socket(ip, port);
        connected = true;
        serverIp = ip;
        serverPort = port;
    }

    void disconnect() throws IOException {
        if(connected){
            connection.close();
            connected = false;
            serverIp = "";
            serverPort = -1;
        }
    }

    boolean isConnected(){
        return connected;
    }

    List<Integer> receiveArrayData() throws IOException {
        DataInputStream in = new DataInputStream(connection.getInputStream());

        int size = in.readInt();
        List<Integer> list = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            list.add(in.readInt());
        }

        return list;
    }

    int receiveInt() throws IOException {
        DataInputStream in = new DataInputStream(connection.getInputStream());

        return in.readInt();
    }

    public void sendData(int data) throws IOException {
        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
        out.writeInt(data);
        out.flush();
    }

    public Socket getConnection() {
        return connection;
    }
}
