import java.util.*;

public class Round {
    private ArrayList<Match> matches;
    private Standings prevRoundStandings;
    private int roundNumber;
    private ArrayList<TournamentPlayer> players;
    private ArrayList<List<TournamentPlayer>> brackets;
    private ArrayList<TournamentPlayer> stillplaying;

    public Round(int roundNumber, ArrayList<TournamentPlayer> tournamentPlayers){
        players = tournamentPlayers;
        stillplaying = players;
        this.roundNumber = roundNumber;
        
    }

    public ArrayList<Match> getRoundMatches(){
        return matches;
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

    public ArrayList<List<TournamentPlayer>> getBracket(){
        return brackets;
    }


    public void generateMatches(ArrayList<Match> m){
        this.createBrackets(new ArrayList<List<TournamentPlayer>>());
    
        //this.matches = matches;

        
        boolean isMatched;

        for ( List<TournamentPlayer> bracket : brackets ){
            
            List<TournamentPlayer> bracketcopy = new ArrayList<TournamentPlayer>(bracket);
            Collections.shuffle(bracketcopy);
            HashSet<TournamentPlayer> matched = new HashSet<TournamentPlayer>();

            for (TournamentPlayer p1: bracket){
                
                //TournamentPlayer p1 = bracket.get(0);
                Iterator<TournamentPlayer> bracketIt = bracketcopy.iterator();
                isMatched = false;
                

                while(bracketIt.hasNext() && !isMatched && !matched.contains(p1)){
                    TournamentPlayer p2 = bracketIt.next();
                    if (!p1.hasPlayed(p2) && !p1.equals(p2) && !matched.contains(p2)){
                        matches.add(new Match(p1, p2));
                        matched.add(p2);
                        matched.add(p1);
                        isMatched = true;
                    }

                }

                // if (!isMatched){
                //     matches.add(new Match(p1, bracketcopy.get(bracketcopy.size()-1)));

                //     matched.add(p1);
                //     matched.add(bracketcopy.get(bracketcopy.size()-1));
                    
                // }
                
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

        matches = new ArrayList<Match>();

        this.brackets = brackets;
        stillplaying =  new ArrayList<TournamentPlayer>(prevRoundStandings.getSortedPlayers());


        stillplaying.removeIf(p -> !p.isPlaying());

        if (stillplaying.size()%2  == 1){
            matches.add(new Match(stillplaying.get(0)));
            stillplaying.remove(0);
            
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
