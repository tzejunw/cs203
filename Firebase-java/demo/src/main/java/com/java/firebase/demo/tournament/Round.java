package com.java.firebase.demo.tournament;

import java.util.*;

public class Round {
    private ArrayList<Match> matches;
    private Standings prevRoundStandings;
    private int roundNumber;
    private ArrayList<TournamentPlayer> players;
    private ArrayList<List<TournamentPlayer>> brackets;
    private ArrayList<TournamentPlayer> stillplaying;

    public Round() {
        // Initialize your fields if needed, or leave empty
    }

    public Round(int roundNumber, ArrayList<TournamentPlayer> tournamentPlayers){
        players = tournamentPlayers;
        stillplaying = players;
        this.roundNumber = roundNumber;
        
    }
    
    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public int getRoundNumber(){
        return roundNumber;
    }
    

    public ArrayList<Match> getRoundMatches(){
        return matches;
    }

    public void setRoundMatches(ArrayList<Match> matches) {
        this.matches = matches;
    }
    

    public boolean removePlayer( List<TournamentPlayer> a, TournamentPlayer p){

        int i = 0;
        while ( i < a.size()){
            TournamentPlayer q = a.get(i);
            if (p.getUserID().equals(q.getUserID())){
                a.remove(i);
                return true;
            }
            i++;
        }
        return false;
    }


    public void generateMatches(ArrayList<Match> matches){
        this.createBrackets(new ArrayList<List<TournamentPlayer>>());
    
        this.matches = matches;

        
        boolean isMatched;

        for ( List<TournamentPlayer> bracket : brackets ){
            
            List<TournamentPlayer> bracketcopy = new ArrayList<TournamentPlayer>(bracket);
            Collections.shuffle(bracketcopy);
            HashSet<TournamentPlayer> matched = new HashSet<TournamentPlayer>();
            //while (bracket.size() != 2)
            for (TournamentPlayer p1: bracket){
                

                
                //TournamentPlayer p1 = bracket.get(0);
                Iterator<TournamentPlayer> bracketIt = bracketcopy.iterator();
                isMatched = false;
                

                while(bracketIt.hasNext() && !isMatched && !matched.contains(p1)){
                    TournamentPlayer p2 = bracketIt.next();
                    if (!p1.hasPlayed(p2) && !p1.equals(p2) && !matched.contains(p2) && !matched.contains(p1)){
                        matches.add(new Match(p1, p2));
                        matched.add(p2);
                        matched.add(p1);
                        isMatched = true;
                    }
                    // if ( bracketcopy.contains(p1) || bracketcopy.contains(p2) || bracket.contains(p1) || bracket.contains(p2)){
                    //     System.out.println("fck");
                    // }


                }

                if (!isMatched){
                    matches.add(new Match(p1, bracketcopy.get(bracketcopy.size()-1)));

                    matched.add(p1);
                    matched.add(bracketcopy.get(bracketcopy.size()-1));
                    
                }
                
            }

            // matches.add(new Match(bracket.get(0), bracketcopy.get(1)));
        }
        
    }

    public Standings generateStandings(){
        prevRoundStandings = new Standings(players);
        return prevRoundStandings;
    }

    public void generateRoundOne(){
        Collections.shuffle(players);

        if (players.size() % 2 == 1){
            matches.add(new Match(players.get(players.size()-1)));
        }

        for (int i = 0; i < players.size()/2; i++){
            matches.add(new Match(players.get(i), players.get(i + players.size()/2)));
        }
    }

    public void createBrackets(ArrayList<List<TournamentPlayer>> brackets){

        this.brackets = brackets;
        stillplaying =  new ArrayList<TournamentPlayer>(prevRoundStandings.getSortedPlayers());


        stillplaying.removeIf(p -> !p.isPlaying());

        if (stillplaying.size()%2  == 1){
            matches.add(new Match(stillplaying.get(stillplaying.size()-1)));
            stillplaying.remove(stillplaying.size()-1);
            
        }

        int curPts = stillplaying.get(0).getTotalGamePoints();
        
        List<TournamentPlayer> bracket = new ArrayList<TournamentPlayer>(); 

        Iterator<TournamentPlayer> plIterator = stillplaying.iterator();
        TournamentPlayer p;

        while (plIterator.hasNext()){

            p = plIterator.next();
            
            if (curPts != p.getCurMatchPts()){
                curPts = p.getCurMatchPts();
                if (bracket.size()%2 == 1){
                    bracket.add(p);
                    brackets.add(bracket);
                    bracket = new ArrayList<TournamentPlayer>();
                }else{
                    brackets.add(bracket);
                    bracket = new ArrayList<TournamentPlayer>();
                    bracket.add(p);
                }
                
            }else{
                bracket.add(p);
            }

        }

        brackets.add(bracket);
    }

    public Standings getStandings(){
        return prevRoundStandings;
    }



}


// import lombok.Getter;
// import lombok.Setter;
// import java.util.List;

// @Setter
// @Getter

// public class Round {
//     private String roundName;
//     private List<Match> matches;
//     private List<Standing> standings;
// }
