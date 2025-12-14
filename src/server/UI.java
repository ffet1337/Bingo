package server;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class UI extends JFrame {
    private JPanel serverArea;
    private JPanel serverIpPort;
    private JPanel serverControlArea;
    private JButton serverStartButton;
    private JLabel serverStatus;
    private JTextField serverIpInput;
    private JTextField serverPortInput;
    private JLabel labelIp;
    private JButton serverRandonPortButton;
    private JCheckBox serverLocalhostOption;
    private JPanel mainPanel;
    private ServerConnection sc;

    UI() throws IOException {
        //frame configuration
        add(mainPanel);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        pack();
        setVisible(true);

        //server configuration
        sc = new ServerConnection();

        serverStartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (sc.getStatus() == ServerConnection.ServerStatus.WAITING_CONFIGURATION) {
                        sc.startReceiving(Integer.parseInt(serverPortInput.getText()));
                        serverStartButton.setText("Parar");
                        serverStatus.setText("Recebendo Conexões");
                    }
                    if (sc.getStatus() == ServerConnection.ServerStatus.RECEIVING_CONNECTIONS) {
                        sc.stopReceiving();
                        serverStartButton.setText("Iniciar");
                        serverStatus.setText("Esperando configuração");
                    }
                }
                catch (NumberFormatException ex){
                    serverStatus.setText("Porta inválida");
                }
            }
        });
    }
}
