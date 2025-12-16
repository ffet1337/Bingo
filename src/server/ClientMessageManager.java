package server;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientMessageManager implements Runnable{
    Socket s;
    ConnectionListener listener;

    ClientMessageManager(Socket s, ConnectionListener listener){
        this.s = s;
        this.listener = listener;
    }

    @Override
    public void run() {
        try{
            DataInputStream in = new DataInputStream(s.getInputStream());

            System.out.println("Esperando pelo client");
            int data = in.readInt();

            if(data == -1){
                System.out.println("client desconenctou");
                listener.onClientDisconnected(s);
            }
        }catch (IOException e){
            System.out.println("O servidor perdeu a conexão no runnable");
        }

    }
}
