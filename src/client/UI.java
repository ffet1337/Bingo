package client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Socket;

public class UI extends JFrame{
    private JTextField gameIpInput;
    private JTextField gamePortInput;
    private JPanel gamePortIpArea;
    private JPanel GameArea;
    private JLabel labelIp;
    private JLabel labelPort;
    private JPanel bingoCard;
    private JPanel A1;
    private JPanel A2;
    private JPanel A3;
    private JPanel B1;
    private JPanel B2;
    private JPanel B3;
    private JPanel C1;
    private JPanel C2;
    private JPanel C3;
    private JPanel gameStatusPanel;
    private JPanel main;
    private JButton connectionButton;
    private JLabel gameStatusLabel;

    private Socket connection;
    private boolean connected;

    public UI() {
        //frame configuration
        add(main);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        pack();
        setVisible(true);

        //connection configuration
        connected = false;
        connectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (connected) {
                        connection = null;
                        connected = false;
                        connectionButton.setText("Conectar");
                        gameStatusLabel.setText("Desconectado");
                    } else {
                        String ip = gameIpInput.getText();
                        int port = Integer.parseInt(gamePortInput.getText());
                        connection = new Socket(ip, port);
                        connected = true;
                        connectionButton.setText("Desconectar");
                        gameStatusLabel.setText("Conectado: " + ip + "/" + port);
                    }
                }catch (Exception ex)
                {
                    gameStatusLabel.setText("Conexão falhou");
                }
            }
        });
    }
}
