package com.java.firebase.demo.tournament;

import java.util.*;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Standings {
    
    ArrayList<TournamentPlayer> TournamentPlayers;

    public Standings() {
        TournamentPlayers = new ArrayList<>();
    }

    public Standings(ArrayList<TournamentPlayer> TournamentPlayers){
        this.TournamentPlayers= TournamentPlayers;
        updateCurPoints();
        updateStandings();
    }

    public void updateCurPoints(){
        for (TournamentPlayer player : TournamentPlayers){
            player.updateCurPoints();
        }
        for (TournamentPlayer player : TournamentPlayers){
            player.updateOpponentWinPercents();
        }

    }

    public void updateStandings(){
        Collections.sort( TournamentPlayers, new playerComparator());
    }

    public List<TournamentPlayer> getSortedPlayers(){
        return TournamentPlayers;
    }



}

