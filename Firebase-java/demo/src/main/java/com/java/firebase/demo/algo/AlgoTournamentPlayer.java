package com.java.firebase.demo.algo;
import java.util.ArrayList;
import java.util.List;

public class AlgoTournamentPlayer{

    private List<AlgoMatch> pastMatches;
    private String playerID;

    private int curMatchPts;
    private double curOMW;
    private int curGamePts;
    private double curOGW;
    private boolean stillPlaying;
    
    private final int matchWinPts = 3;
    private final int gameWinPts = 3;
    private final int drawPts = 1;
    private final double minOMW = 0.33;

    public AlgoTournamentPlayer( String userID, List<AlgoMatch> matches){
        pastMatches = matches;
        playerID = userID;
        stillPlaying = true;

    }

    public String getPlayerID(){
        return playerID;
    }

    public int getCurMatchPts() {
        return curMatchPts;
    }

    public double getCurOMW() {
        return curOMW;
    }

    public double getCurOGW() {
        return curOGW;
    }

    public void updateCurPoints(){
        curMatchPts = getTotalMatchPoints();
        curGamePts = getTotalGamePoints();
    }


    public void updateOpponentWinPercents(){
        curOGW = getOGW();
        curOMW = getOMW();
    }

    public void addMatch( AlgoMatch match){
        pastMatches.add(match);
    }

    public int getTotalMatchPoints(){

        int matchWins = 0;

        // iterate through prev matches and count

        for ( AlgoMatch m : pastMatches){
            if (m.getWinner() == this){
                matchWins++;
            }
        }

        return matchWins * matchWinPts;
    }

    public int getTotalGamePoints(){

        // iterate through prev matches and count

        int gameWins = 0;

        for ( AlgoMatch m: pastMatches){
            if (m.getWinner().equals(this)){
                gameWins += m.getGameWins();
            }else{
                gameWins +=  m.getGameLosses();
            }
        }

        return gameWins * gameWinPts;
    }

    public int getDraws(){

        int draws = 0;

        for ( AlgoMatch m: pastMatches){
            if (m.isDraw()){
                draws++;
            }
        }
        return draws;
    }

    public int roundsPlayed(){
        return pastMatches.size() - getByes();
    }

    public int getByes(){
        int byes = 0;
        for ( AlgoMatch m : pastMatches){
            if (m.isBye()){
                byes++;
            }
        }
        return byes;
    }

    public double getCurGW(){
        return getTotalGamePoints() / ((double)roundsPlayed() * 3);
    }

    public double getOMW(){

        double OMW = 0;

        for ( AlgoMatch m : pastMatches){
            if (!m.isBye()){
                AlgoTournamentPlayer opp = m.getPlayer1().equals(this) ? m.getPlayer2() : m.getPlayer1();
                double oppOMW = (double)opp.getTotalMatchPoints() / opp.roundsPlayed() > minOMW ? (double)opp.getTotalMatchPoints() / opp.roundsPlayed() : minOMW;  
                OMW +=  oppOMW;    
            }
        }

        return OMW / roundsPlayed();
    }

    public double getOGW(){

        double OGW = 0;

        for ( AlgoMatch m : pastMatches){
            if (!m.isBye()){
                AlgoTournamentPlayer opp = m.getPlayer1().equals(this) ? m.getPlayer2() : m.getPlayer1();
                
                OGW += opp.getCurGW() < minOMW ? minOMW : getCurGW();    
            }
        }

        return OGW / roundsPlayed();
    }

    public void dropPlayer(){
        stillPlaying = false;
    }

    public boolean isPlaying(){
        return stillPlaying;
    }

    public boolean hasPlayed(AlgoTournamentPlayer player){
        for ( AlgoMatch m : pastMatches){
            if (player.equals(m.getPlayer1()) || player.equals(m.getPlayer2())){
                return true;
            }
        }
        return false;
    }

    public void printmatches(){
        for (AlgoMatch m : pastMatches){
            System.out.println(m.getPlayer1().getPlayerID() +" vs " + m.getPlayer2().getPlayerID());
        }
    }
}
