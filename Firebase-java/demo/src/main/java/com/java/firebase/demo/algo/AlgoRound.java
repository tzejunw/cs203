package com.java.firebase.demo.algo;
import java.util.*;



public class AlgoRound {
    private ArrayList<AlgoMatch> AlgoMatches;
    private AlgoStandings prevRoundStandings;
    private int roundNumber;
    private ArrayList<AlgoTournamentPlayer> players;
    private ArrayList<List<AlgoTournamentPlayer>> brackets;
    private ArrayList<AlgoTournamentPlayer> stillplaying;

    public AlgoRound(int roundNumber, ArrayList<AlgoTournamentPlayer> AlgoTournamentPlayers){
        players = AlgoTournamentPlayers;
        stillplaying = players;
        this.roundNumber = roundNumber;
        this.AlgoMatches = new ArrayList<AlgoMatch>();
        
    }

    public ArrayList<AlgoMatch> getAlgoMatches(){
        return AlgoMatches;
    }

    public boolean removePlayer( List<AlgoTournamentPlayer> a, AlgoTournamentPlayer p){

        int i = 0;
        while ( i < a.size()){
            AlgoTournamentPlayer q = a.get(i);
            if (p.getPlayerID().equals(q.getPlayerID())){
                a.remove(i);
                return true;
            }
            i++;
        }
        return false;
    }

    public ArrayList<List<AlgoTournamentPlayer>> getBracket(){
        return brackets;
    }


    public void generateAlgoMatches(){
        this.createBrackets(new ArrayList<List<AlgoTournamentPlayer>>());
    
        //this.AlgoMatches = AlgoMatches;

        
        boolean isAlgoMatched;

        for ( List<AlgoTournamentPlayer> bracket : brackets ){
            
            List<AlgoTournamentPlayer> bracketcopy = new ArrayList<AlgoTournamentPlayer>(bracket);
            Collections.shuffle(bracketcopy);
            HashSet<AlgoTournamentPlayer> AlgoMatched = new HashSet<AlgoTournamentPlayer>();

            for (AlgoTournamentPlayer p1: bracket){
                
                Iterator<AlgoTournamentPlayer> bracketIt = bracketcopy.iterator();
                isAlgoMatched = false;
                

                while(bracketIt.hasNext() && !isAlgoMatched && !AlgoMatched.contains(p1)){
                    AlgoTournamentPlayer p2 = bracketIt.next();
                    if (!p1.hasPlayed(p2) && !p1.equals(p2) && !AlgoMatched.contains(p2)){
                        AlgoMatches.add(new AlgoMatch(p1, p2));
                        AlgoMatched.add(p2);
                        AlgoMatched.add(p1);
                        isAlgoMatched = true;
                    }

                }
                
            }

        }
        
    }

    public AlgoStandings generateStandings(){
        prevRoundStandings = new AlgoStandings(players);
        return prevRoundStandings;
    }

    public void generateRoundOne(){
        Collections.shuffle(players);

        if (players.size() % 2 == 1){
            AlgoMatches.add(new AlgoMatch(players.get(players.size()-1)));
        }

        for (int i = 0; i < players.size()/2; i++){
            AlgoMatches.add(new AlgoMatch(players.get(i), players.get(i + players.size()/2)));
        }
    }

    public void createBrackets(ArrayList<List<AlgoTournamentPlayer>> brackets){

        this.brackets = brackets;
        stillplaying =  new ArrayList<AlgoTournamentPlayer>(prevRoundStandings.getStandings());


        stillplaying.removeIf(p -> !p.isPlaying());

        if (stillplaying.size()%2  == 1){
            AlgoMatches.add(new AlgoMatch(stillplaying.get(0)));
            stillplaying.remove(0);
            
        }

        int curPts = stillplaying.get(0).getTotalGamePoints();
        
        List<AlgoTournamentPlayer> bracket = new ArrayList<AlgoTournamentPlayer>(); 

        Iterator<AlgoTournamentPlayer> plIterator = stillplaying.iterator();
        AlgoTournamentPlayer p;

        while (plIterator.hasNext()){

            p = plIterator.next();
            
            if (curPts != p.getCurMatchPts()){
                curPts = p.getCurMatchPts();
                if (bracket.size()%2 == 1){
                    bracket.add(p);
                    brackets.add(bracket);
                    bracket = new ArrayList<AlgoTournamentPlayer>();
                }else{
                    brackets.add(bracket);
                    bracket = new ArrayList<AlgoTournamentPlayer>();
                    bracket.add(p);
                }
                
            }else{
                bracket.add(p);
            }

        }

        brackets.add(bracket);
    }

    public AlgoStandings getStandings(){
        return prevRoundStandings;
    }



}

