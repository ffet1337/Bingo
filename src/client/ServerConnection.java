package client;

public class ServerConnection {
    private ServerStatus status;

    enum ServerStatus{
        RECEIVING_CONNECTIONS,
        WAITING_CONFIGURATION,
        STARTING_SERVER,
        ERROR
    }

    ServerConnection(){

    }

    void startReceiving(){

    }

    void stopReceiving(){

    }

    ServerStatus getStatus(){

    }
}
