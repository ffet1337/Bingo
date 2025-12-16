package server;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
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
        setGameInput(false);
        pack();
        setVisible(true);

        //lists
        bingoPlayersList.setModel(playersListModel);
        bingoNumbersList.setModel(numbersListModel);

        //server configuration
        serverStartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (sc.getStatus() == ServerConnection.ServerStatus.WAITING_CONFIGURATION) {
                        serverStart();
                    }else if (sc.getStatus() == ServerConnection.ServerStatus.RECEIVING_CONNECTIONS) {
                        serverStop();
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
        });

        bingoStartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if(!inGame){
                        bingoStart();
                        serverStartButton.setEnabled(false);
                        bingoStartButton.setText("Encerrar");
                    }
                    else{
                        bingoStop();
                        serverStartButton.setEnabled(true);
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
        serverRandonPortButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int porta = 49152 + (int)(Math.random() * (65535 - 49152 + 1));
                serverPortInput.setText(String.valueOf(porta));
            }
        });
        serverLocalhostOption.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (serverIpInput.getText().equals("127.0.0.1")) {
                        Enumeration<NetworkInterface> interfaces =
                                NetworkInterface.getNetworkInterfaces();
                        boolean found = false;
                        while (interfaces.hasMoreElements() && !found) {
                            NetworkInterface ni = interfaces.nextElement();

                            // ignora interfaces desligadas ou loopback
                            if (!ni.isUp() || ni.isLoopback())
                                continue;

                            Enumeration<InetAddress> addresses = ni.getInetAddresses();
                            while (addresses.hasMoreElements()) {
                                InetAddress addr = addresses.nextElement();

                                if (addr instanceof Inet4Address) {
                                   serverIpInput.setText(addr.getHostAddress());
                                   found = true;
                                }
                            }
                        }
                    }else{
                        serverIpInput.setText("127.0.0.1");
                    }
                }catch (SocketException ex){
                    System.out.println("Erro tentando acessar o IPV4 da maquina");
                }
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Executando tarefas antes de fechar o server...");

                if(inGame){
                    try {
                        bingoStop();
                    } catch (IOException ex) {
                        System.out.println("não foi possivel encerrar o jogo antes fechar a janela");
                    }
                }

                try {
                    serverStop();
                } catch (IOException ex) {
                    System.out.println("não foi possivel encerrar o servidor antes fechar a janela");
                }

                dispose();
                System.exit(0);
            }
        });
    }

    private void serverStart() throws IOException {
        sc.startReceiving(Integer.parseInt(serverPortInput.getText()));

        serverIpInput.setEnabled(false);
        serverPortInput.setEnabled(false);
        serverRandonPortButton.setEnabled(false);
        serverLocalhostOption.setEnabled(false);

        serverStartButton.setText("Parar");
        serverStatus.setText("Recebendo Conexões");

        setBingoControls(false);
    }

    private void serverStop() throws IOException {
        sc.broadcastData(1339);
        for(int i = sc.getConnectionsCount() - 1; i >= 0; i--){
            sc.removeClient(i);
        }
        connectedIdsTrack.clear();
        socketInfoTrack.clear();
        numbersListModel.clear();
        playersListModel.clear();

        sc.stopReceiving();

        serverIpInput.setEnabled(true);
        serverPortInput.setEnabled(true);
        serverRandonPortButton.setEnabled(true);
        serverLocalhostOption.setEnabled(true);

        setGameInput(false);
        serverStartButton.setText("Iniciar");
        serverStatus.setText("Esperando configuração");

        setBingoControls(false);
        game = null;
    }

    private void bingoStart() throws IOException {
        sc.broadcastData(1337);
        game = new Bingo();

        List<Integer> hold;
        for(int i = 0; i < sc.getConnectionsCount(); i++){
            game.addPlayer(String.valueOf(connectedIdsTrack.get(i)));
            hold = game.generateCardForPlayer(String.valueOf(connectedIdsTrack.get(i)));
            sc.sendDataArrayTo(i, hold);
        }

        setBingoControls(true);
        inGame = true;
    }

    private void bingoStop() throws IOException {
        sc.broadcastData(1338);
        setBingoControls(false);
        inGame = false;
        numbersListModel.clear();

        game = null;
    }

    private void selectNumber(int number) throws IOException {
        game.addSelectedNumber(number);
        numbersListModel.addElement(String.valueOf(number));
        sc.broadcastData(number);
    }

    @Override
    public void onClientConnected(Socket s) throws IOException {
        if(socketInfoTrack.isEmpty()){
            bingoStartButton.setEnabled(true);
            setBingoControls(false);
        }

        playersListModel.addElement("Jogador " + idTrack + " :" + s.getRemoteSocketAddress().toString());

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
        int idHold = -1;
        String socketHold = "";
        for(int i = 0; i < socketInfoTrack.size(); i++){
            if(socketInfoTrack.get(i).equals(s.getRemoteSocketAddress().toString())){
                idHold = connectedIdsTrack.remove(i);
                socketHold = socketInfoTrack.remove(i);
                break;
            }
        }

        playersListModel.removeElement("Jogador " + idHold + " :" + socketHold);

        sc.removeClient(s);
        gameStatus.setText("Cliente " + s.getRemoteSocketAddress().toString() + " saiu do jogo: " + "Jogador " + idHold);

        if(socketInfoTrack.isEmpty()){
            setGameInput(false);
        }
    }

    private void setServerInput(boolean b){
        serverStartButton.setEnabled(b);
        serverStartButton.setFocusable(b);

        serverPortInput.setFocusable(b);
        serverPortInput.setEditable(b);

        serverRandonPortButton.setEnabled(b);
        serverRandonPortButton.setFocusable(b);

        serverLocalhostOption.setEnabled(b);
    }

    private void setGameInput(boolean b){
        bingoNumberInput.setEditable(b);
        bingoNumberInput.setFocusable(b);

        bingoStartButton.setFocusable(b);
        bingoStartButton.setEnabled(b);

        bingoRandomNumberButton.setEnabled(b);
        bingoRandomNumberButton.setFocusable(b);

        bingoSendButton.setEnabled(b);
        bingoSendButton.setFocusable(b);
    }

    private void setBingoControls(boolean b){
        bingoSendButton.setEnabled(b);
        bingoSendButton.setFocusable(b);

        bingoRandomNumberButton.setFocusable(b);
        bingoRandomNumberButton.setEnabled(b);

        bingoNumberInput.setFocusable(b);
        bingoNumberInput.setEditable(b);
    }
}