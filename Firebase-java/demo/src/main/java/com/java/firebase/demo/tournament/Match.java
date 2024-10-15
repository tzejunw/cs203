package com.java.firebase.demo.tournament;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Match {
    private TournamentPlayer player1;
    private TournamentPlayer player2;
    private TournamentPlayer winner;
    private int gameWins;
    private int gameLosses;
    private Boolean isDraw;
    private Boolean isBye = false;

    public Match() {
        // Initialize your fields if needed, or leave empty
    }

    public Match(TournamentPlayer player1, TournamentPlayer player2){
        this.player1 = player1;
        this.player2 = player2;

    }

    public Match(TournamentPlayer player){
        player1 = player;
        isBye = true;
        gameWins = 2;
        gameLosses = 0;
    }

    public void update(TournamentPlayer winner, int gameWins, int gameLosses){

        this.winner = winner;

        this.gameLosses = gameLosses;
        this.gameWins = gameWins;
        if (gameWins == gameLosses){
            isDraw = true;
        }

        player1.addMatch(this);
        player2.addMatch(this);
    }

    public TournamentPlayer getWinner(){
        return winner;
    }

    public void setWinner(TournamentPlayer winner) {
        this.winner = winner;
    }



    public int getGameWins(){
        return gameWins;
    }

    public int getGameLosses(){
        return gameLosses;
    }


    public String toString(){
        String loser = player1.equals(winner) ? player2.getPlayerID() : player1.getPlayerID();
        return winner.getPlayerID() + " " + loser + " " + gameWins + "-" + gameLosses;
    }
}

// import lombok.Getter;
// import lombok.Setter;

// @Setter
// @Getter

// public class Match {
//     private String player1;
//     private String player2;
//     private String winner;
//     private int wins;
//     private int losses;
//     private boolean isDraw;
//     private boolean isBye;
// }
