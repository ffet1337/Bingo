package client;

import javax.swing.*;

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

    UI()
    {
        add(mainPanel);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setVisible(true);

    }
}
