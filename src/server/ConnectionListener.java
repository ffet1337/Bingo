package server;

import java.io.IOException;
import java.net.Socket;

public interface ConnectionListener{
    void onClientConnected(Socket s) throws IOException;
    void onClientDisconnected(Socket s) throws IOException;
}
