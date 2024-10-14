package com.java.firebase.demo.tournament;

public class Match {
    private TournamentPlayer player1;
    private TournamentPlayer player2;
    private TournamentPlayer winner;
    private int gameWins;
    private int gameLosses;
    private Boolean isDraw;
    private Boolean isBye = false;

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

    public TournamentPlayer getP1(){
        return player1;
    }

    public TournamentPlayer getP2(){
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
        String loser = player1.equals(winner) ? player2.getUserID() : player1.getUserID();
        return winner.getUserID() + " " + loser + " " + gameWins + "-" + gameLosses;
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
