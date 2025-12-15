package server;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class UI extends JFrame implements ConnectionListener {
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
    private JButton bingoStartButton;
    private JTextField bingoNumberInput;
    private JButton bingoRandomNumberButton;
    private JButton bingoSendButton;
    private JPanel gameArea;
    private JPanel playersArea;
    private JPanel bingoArea;
    private JLabel labelPlayer;
    private JLabel labelPorta;
    private JList bingoPlayersList;
    DefaultListModel<String> playersListModel = new DefaultListModel<>();
    private JList bingoNumbersList;
    DefaultListModel<String> numbersListModel = new DefaultListModel<>();
    private JLabel labelBingo;
    private JLabel bingoSelectedNumbers;
    private JLabel numberLaber;
    private JPanel bingoRandomSendArea;
    private JPanel warningArea;
    private JLabel gameStatus;

    private ServerConnection sc = new ServerConnection();
    Bingo game;
    boolean inGame = false;
    int idTrack = 0;

    //these 2 should be trated as one, if one is removed then other should be removed too
    List<String> socketInfoTrack = new ArrayList<>();
    List<Integer> connectedIdsTrack = new ArrayList<>();

    UI() throws IOException {
        //frame configuration
        add(mainPanel);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        pack();
        setVisible(true);

        //lists
        bingoPlayersList.setModel(playersListModel);
        bingoNumbersList.setModel(numbersListModel);

        //server configuration
        serverStartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                serverStart();
            }
        });

        bingoStartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if(!inGame){
                        bingoStart();
                        setServerInput(false);
                        bingoStartButton.setText("Encerrar");
                    }
                    else{
                        bingoStop();
                        setServerInput(true);
                        bingoStartButton.setText("Iniciar");
                    }

                } catch (IOException ex) {
                    ex.printStackTrace();
                    gameStatus.setText("Ocorreu um erro inciando o jogo");
                }
            }
        });

        bingoRandomNumberButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bingoNumberInput.setText(String.valueOf(game.generateRandomNumber()));
            }
        });

        bingoSendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    selectNumber(Integer.parseInt(bingoNumberInput.getText()));
                    if(game.hasWinner()){
                        gameStatus.setText("jogador " + game.getWinner() + " ganhou!!");
                    }else{
                        gameStatus.setText("Número " + Integer.parseInt(bingoNumberInput.getText()) + " selecionado");
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    gameStatus.setText("Ocorreu um erro ao enviar um numero");
                }
                catch (IllegalArgumentException ex1){
                    gameStatus.setText(ex1.getMessage());
                }
            }
        });
        sc.setListener(this);
    }

    private void serverStart(){
        try {
            if (sc.getStatus() == ServerConnection.ServerStatus.WAITING_CONFIGURATION) {
                sc.startReceiving(Integer.parseInt(serverPortInput.getText()));
                serverPortInput.setFocusable(false);
                serverPortInput.setEditable(false);
                serverStartButton.setText("Parar");
                serverStatus.setText("Recebendo Conexões");
                game = new Bingo();
            }else if (sc.getStatus() == ServerConnection.ServerStatus.RECEIVING_CONNECTIONS) {
                sc.stopReceiving();
                serverPortInput.setFocusable(true);
                serverPortInput.setEditable(true);
                serverStartButton.setText("Iniciar");
                serverStatus.setText("Esperando configuração");
                game = null;
            }
        }
        catch (NumberFormatException ex1){
            serverStatus.setText("Porta inválida, use numeros");
        }
        catch (IllegalArgumentException ex2){
            serverStatus.setText("Porta inválida, fora do range");
        }
        catch (IOException ex3) {
            serverStatus.setText("Erro no servidor");
        }
    }

    private void bingoStart() throws IOException {
        List<Integer> hold;
        for(int i = 0; i < sc.getConnectionsCount(); i++){
            game.addPlayer(String.valueOf(connectedIdsTrack.get(i)));
            hold = game.generateCardForPlayer(String.valueOf(connectedIdsTrack.get(i)));
            sc.sendDataArrayTo(i, hold);
        }

        sc.broadcastData(1337);
        inGame = true;
    }

    private void bingoStop() throws IOException {
        sc.broadcastData(1338);
        inGame = false;
    }

    private void selectNumber(int number) throws IOException {
        game.addSelectedNumber(number);
        sc.broadcastData(number);
    }

    @Override
    public void onClientConnected(Socket s) throws IOException {
        playersListModel.addElement(s.getRemoteSocketAddress().toString());

        DataOutputStream out = new DataOutputStream(s.getOutputStream());

        out.writeInt(idTrack);
        out.flush();

        socketInfoTrack.add(s.getRemoteSocketAddress().toString());
        connectedIdsTrack.add(idTrack);

        gameStatus.setText("Cliente " + s.getRemoteSocketAddress().toString() + " entrou no jogo, como jogador " + idTrack);
        idTrack++;
    }

    @Override
    public void onClientDisconnected(Socket s) throws IOException {
        playersListModel.removeElement(s.getRemoteSocketAddress().toString());

        for(int i = 0; i < socketInfoTrack.size(); i++){
            if(socketInfoTrack.get(i).equals(s.getRemoteSocketAddress().toString())){
                connectedIdsTrack.remove(i);
                socketInfoTrack.remove(i);
                break;
            }
        }

        sc.removeClient(s);
        gameStatus.setText("Cliente " + s.getRemoteSocketAddress().toString() + " saiu do jogo");
    }

    private void setServerInput(boolean b){
        serverStartButton.setEnabled(b);
        serverStartButton.setFocusable(b);

        serverPortInput.setFocusable(b);
        serverPortInput.setEditable(b);
    }
}