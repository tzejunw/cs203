package com.java.firebase.demo.tournament;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class TournamentPlayer{

    private List<Match> pastMatches;
    private String playerID;

    private int curMatchPts;
    private double curOMW;
    private int curGamePts;
    private double curOGW;
    private boolean stillPlaying;
    
    private final int matchWinPts = 3;
    private final int gameWinPts = 3;
    private final int drawPts = 1;

    public TournamentPlayer( String userID, List<Match> matches){
        pastMatches = matches;
        playerID = userID;
        stillPlaying = true;

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

    public void addMatch( Match match){
        pastMatches.add(match);
    }

    public int getTotalMatchPoints(){

        //return pastMatches.stream().filter(m -> (getP1().equals(this) && isP1Winner) && (getP2().equals(this) && !isP1Winner)).map(m :: getGameWins).stream().reduce(0, (a, b) -> a + b, Integer::sum);

        int matchWins = 0;

        for ( Match m : pastMatches){
            if (m.getWinner() == this){
                matchWins++;
            }
        }

        return matchWins * matchWinPts;
    }

    public int getTotalGamePoints(){


        int gameWins = 0;

        for ( Match m: pastMatches){
            if (m.getWinner().equals(this)){
                gameWins += m.getGameWins();
            }else{
                gameWins +=  m.getGameLosses();
            }
        }

        return gameWins;
    }

    public int getDraws(){

        int draws = 0;

        for ( Match m: pastMatches){
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
        for ( Match m : pastMatches){
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

        for ( Match m : pastMatches){
            if (!m.isBye()){
                TournamentPlayer opp = m.getP1().equals(this) ? m.getP2() : m.getP1();
                double oppOMW = (double)opp.getTotalMatchPoints() / opp.roundsPlayed() > 0.33 ? (double)opp.getTotalMatchPoints() / opp.roundsPlayed() : 0.33;
                OMW +=  oppOMW;    
            }
        }

        return OMW / roundsPlayed();
    }

    public double getOGW(){

        double OGW = 0;

        for ( Match m : pastMatches){
            if (!m.isBye()){
                TournamentPlayer opp = m.getP1().equals(this) ? m.getP2() : m.getP1();
                
                OGW += opp.getCurGW() < 0.33 ? 0.33 : getCurGW();    
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

    public boolean hasPlayed(TournamentPlayer player){
        for ( Match m : pastMatches){
            if (player.equals(m.getP1()) || player.equals(m.getP2())){
                return true;
            }
        }
        return false;
    }
}

