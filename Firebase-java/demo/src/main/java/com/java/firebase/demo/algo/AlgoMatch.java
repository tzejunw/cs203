package com.java.firebase.demo.algo;


public class AlgoMatch {
    private AlgoTournamentPlayer player1;
    private AlgoTournamentPlayer player2;
    private AlgoTournamentPlayer winner;
    private int gameWins;
    private int gameLosses;
    private Boolean isDraw;
    private Boolean isBye = false;

    public AlgoMatch(AlgoTournamentPlayer player1, AlgoTournamentPlayer player2){
        this.player1 = player1;
        this.player2 = player2;
    }

    public AlgoMatch(AlgoTournamentPlayer player){
        player1 = player;
        winner = player;
        isBye = true;
        gameWins = 2;
        gameLosses = 0;
    }

    public void update(AlgoTournamentPlayer winner, int gameWins, int gameLosses){

        if (!isBye){
            this.winner = winner;

            this.gameLosses = gameLosses;
            this.gameWins = gameWins;
    
            if (gameWins == gameLosses){
                isDraw = true;
            }
    
            player1.addMatch(this);
            player2.addMatch(this);

        }


    }

    

    public AlgoTournamentPlayer getWinner(){
        return winner;
    }

    public AlgoTournamentPlayer getPlayer1(){
        return player1;
    }

    public AlgoTournamentPlayer getPlayer2(){
        return player2;
    }

    public int getGameWins(){
        return gameWins;
    }

    public int getGameLosses(){
        return gameLosses;
    }

    public Boolean isBye(){
        return isBye;
    }

    public Boolean isDraw(){
        return isDraw;
    }

    public String toString(){

        if (isBye){
            return winner.getPlayerID() + " wins with bye";
        }
        String loser = player1.equals(winner) ? player2.getPlayerID() : player1.getPlayerID();
        return winner.getPlayerID() + " " + loser + " " + gameWins + "-" + gameLosses;
    }
}
