package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

public class UI extends JFrame {
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
    private JLabel labelA1;
    private JLabel labelA2;
    private JLabel labelA3;
    private JLabel labelB1;
    private JLabel labelB2;
    private JLabel labelB3;
    private JLabel labelC1;
    private JLabel labelC2;
    private JLabel labelC3;
    private JLabel playerId;

    ClientConnection cs = new ClientConnection();
    List<Integer> card;
    boolean waitingServerStart;
    boolean inGame;

    public UI() {
        //frame configuration
        add(main);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        pack();
        setVisible(true);

        //connection configuration
        waitingServerStart = false;
        inGame = false;
        connectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (!cs.isConnected()) {
                        cs.connect(gameIpInput.getText(), Integer.parseInt(gamePortInput.getText()));
                        setInput(false);
                        associatePlayer();
                        startWaiting();
                        connectionButton.setText("Desconectar");
                        gameStatusLabel.setText("Conectado");
                    } else {
                        exitServer();
                        cs.disconnect();
                        setInput(true);
                        connectionButton.setText("Conectar");
                        gameStatusLabel.setText("Jogo cancelado pelo cliente");
                    }
                }catch (Exception ex)
                {
                    gameStatusLabel.setText("Conexão falhou");
                }
            }
        });
    }

    void startWaiting() throws IOException {
        waitingServerStart = true;
        
        Thread t = new Thread(this::waiting);
        t.start();
    }

    void waiting(){
        try{
            inGame = false;
            connectionButton.setFocusable(true);
            connectionButton.setEnabled(true);
            setInput(true);
            waitingServerStart = true;

            int aux;
            DataInputStream in = new DataInputStream(cs.getConnection().getInputStream());

            aux = in.readInt();
            // 1337 is the code for the game starting
            if(aux == 1337){
                configureCard();
                inGame = true;
                waitingServerStart = false;
                gameStatusLabel.setText("O jogo foi iniciado");
                setInput(false);
                connectionButton.setFocusable(false);
                connectionButton.setEnabled(false);
                startReceivingNumbers();
            }

            if(aux == 1339){
                waitingServerStart = false;
                cs.disconnect();
                connectionButton.setText("Connectar");
                gameStatusLabel.setText("O server cancelou a conexão");
            }

        }catch (IOException e){
            System.out.print(e.getMessage() + "\n");
        }
    }

    void startReceivingNumbers() throws IOException {
        while (inGame){
            int aux;
            DataInputStream in = new DataInputStream(cs.getConnection().getInputStream());
            aux = in.readInt();

            //if the code receivied is 1338 the game is declared ended
            if(aux == 1338){
                gameStatusLabel.setText("O servidor encerrou o jogo");
                waiting();
                return;
            }

            gameStatusLabel.setText("Número " + aux + " selecionado");

            if(card.contains(aux)){
                int cardCount = 0;
                for(int i = 0; i < bingoCard.getComponentCount(); i++){
                    Component c = bingoCard.getComponent(i);
                    if(c instanceof JPanel){
                        JLabel l = (JLabel) ((JPanel) c).getComponent(0);
                        int labelValue = Integer.parseInt(l.getText());
                        if(labelValue == aux){
                            ((JPanel) c).setBackground(Color.YELLOW);
                        }
                        cardCount++;
                    }
                }
            }
        }
    }

    private void exitServer() throws IOException {
        cs.sendData(-1);
    }

    private void associatePlayer() throws IOException {
        int id = cs.receiveInt();
        playerId.setText("Jogador " + id);
    }

    private void configureCard() throws IOException {
        card = cs.receiveArrayData();
        int cardCount = 0;
        for(int i = 0; i < bingoCard.getComponentCount(); i++){
            Component c = bingoCard.getComponent(i);
            if(c instanceof JPanel){
                ((JPanel) c).setBackground(Color.WHITE);
                JLabel l = (JLabel) ((JPanel) c).getComponent(0);
                l.setText(String.valueOf(card.get(cardCount)));
                cardCount++;
            }
        }
    }

    private void setInput(boolean b){
        gameIpInput.setFocusable(b);
        gameIpInput.setEditable(b);
        gamePortInput.setFocusable(b);
        gamePortInput.setEditable(b);
    }
}
