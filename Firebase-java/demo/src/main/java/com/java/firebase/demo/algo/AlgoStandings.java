package com.java.firebase.demo.algo;
import java.util.*;

public class AlgoStandings {
    
    ArrayList<AlgoTournamentPlayer> TournamentPlayers;

    public AlgoStandings(ArrayList<AlgoTournamentPlayer> TournamentPlayers){
        this.TournamentPlayers= TournamentPlayers;
        updateCurPoints();
        updateStandings();
    }

    public void updateCurPoints(){
        for (AlgoTournamentPlayer player : TournamentPlayers){
            player.updateCurPoints();
        }
        for (AlgoTournamentPlayer player : TournamentPlayers){
            player.updateOpponentWinPercents();
        }

    }

    public void updateStandings(){
        Collections.sort( TournamentPlayers, new playerComparator());
    }

    public List<AlgoTournamentPlayer> getStandings(){
        return TournamentPlayers;
    }





}
