public class Match {
    private TournamentPlayer player1;
    private TournamentPlayer player2;
    private Boolean p1winner;
    private int gameWins;
    private int gameLosses;
    private Boolean isDraw;
    private Boolean isBye;

    public Match(TournamentPlayer player1, TournamentPlayer player2){
        this.player1 = player1;
        this.player2 = player2;

    }

    public void update(TournamentPlayer winner, int gameWins, int gameLosses, Boolean bye){

        p1winner = winner.equals(player1) ? true : false;

        this.gameLosses = gameLosses;
        this.gameWins = gameWins;
        if (gameWins == gameLosses){
            isDraw = true;
        }
    }

    public TournamentPlayer getP1(){
        return player1;
    }

    public TournamentPlayer getP2(){
        return player2;
    }

    public boolean isP1Winner(){
        return p1winner;
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
}
