package server;

import java.util.ArrayList;
import java.util.List;

public class Bingo {
    //players are assigned to its cards using the list index;
    //when removing one player of the game, his card must be removed too

    final int numbersPerCard = 9;
    final int maxNumber = 25;
    private List<String> players;
    private List<List<Integer>> cards;
    private List<Integer> selectedNumbers;
    private List<Integer> playerPoints;
    boolean hasWinner;
    int winner = -1;

    Bingo(){
        this.players = new ArrayList<>();
        this.cards = new ArrayList<>();
        this.selectedNumbers = new ArrayList<>();
        this.playerPoints = new ArrayList<>();
    }

    void addPlayer(String id)
    {
        players.add(id);
        playerPoints.add(0);
    }

    void addSelectedNumber(int num){
        if(selectedNumbers.contains(num))
        {
            throw new IllegalArgumentException("Esse número ja foi selecionado");
        }
        if(num < 0 || num > 25){
            throw new IllegalArgumentException("Esse numero esta fora do range (0 - 25)");
        }
        selectedNumbers.add(num);

        for(int i = 0; i < cards.size(); i++){
            for(int j = 0; j < cards.get(i).size(); j++){
                if(num == cards.get(i).get(j)){
                    playerPoints.set(i, playerPoints.get(i) + 1);
                    if(playerPoints.get(i).equals(numbersPerCard)){
                        hasWinner = true;
                        winner = i;
                    }

                }
            }
        }

    }

    int generateRandomNumber()
    {
        while(true){
            int randomNum = (int)(Math.random() * (maxNumber+1));

            if(!selectedNumbers.contains(randomNum)){
                return randomNum;
            }
        }
    }

    public void reset(){
    }

    public boolean hasWinner(){
        return hasWinner;
    }

    public String getWinner(){
        if(winner == -1){
            return "";
        }

        return players.get(winner);
    }

    public List<Integer> generateCardForPlayer(String playerId){
        List<Integer> r = new ArrayList<>();

        while(r.size() < numbersPerCard){
            int randomNum = (int)(Math.random() * (maxNumber+1));

            if(!r.contains(randomNum)){
                r.add(randomNum);
            }
        }
        cards.add(r);
        return r;
    }
}
