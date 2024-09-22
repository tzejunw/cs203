import java.util.*;

public class Round {
    private List<Match> matches;
    private Standings prevRoundStandings;
    private int roundNumber;
    private List<TournamentPlayer> players;
    private List<List<TournamentPlayer>> brackets;
    private List<TournamentPlayer> stillplaying;

    public Round(int roundNumber, List<TournamentPlayer> tournamentPlayers){
        players = tournamentPlayers;
        stillplaying = players;
        this.roundNumber = roundNumber;
        
    }


    public void generateMatches(Standings standing){
        createBrackets();
        boolean isMatched;

        for ( List<TournamentPlayer> bracket : brackets ){
            
            List<TournamentPlayer> bracketcopy = new ArrayList<TournamentPlayer>(bracket);
            Collections.shuffle(bracketcopy);

            while (bracket.size() != 2){
                
                TournamentPlayer p1 = bracket.getFirst();
                Iterator<TournamentPlayer> bracketIt = bracketcopy.iterator();
                isMatched = false;
                

                while(bracketIt.hasNext() && !isMatched){
                    TournamentPlayer p2 = bracketIt.next();
                    if (!p1.hasPlayed(p2) && !p1.equals(p2)){
                        matches.add(new Match(p1, p2));
                        bracket.removeFirst();
                        bracket.remove(p2);
                        bracketcopy.removeFirst();
                        bracketcopy.remove(p2);
                        isMatched = true;
                    }
                }

                if (!isMatched){
                    matches.add(new Match(p1, bracketcopy.getLast()));
                    bracketcopy.removeLast();
                    bracket.removeFirst();
                }
                
            }

            matches.add(new Match(bracket.getFirst(), bracketcopy.getLast()));
        }
        
    }

    public void generateStandings(){
        prevRoundStandings = new Standings(players);
    }

    public void createBrackets(){

        stillplaying =  new ArrayList<TournamentPlayer>(prevRoundStandings.getSortedPlayers());

        stillplaying.removeIf(p -> !p.isPlaying());

        if (stillplaying.size()%2  == 1){
            matches.add(new Match(stillplaying.getLast()));
            stillplaying.removeLast();
        }

        int curPts = stillplaying.getFirst().getTotalGamePoints();
        int curBracketSize = 0;
        List<TournamentPlayer> bracket = new ArrayList<TournamentPlayer>(); 

        Iterator<TournamentPlayer> plIterator = stillplaying.iterator();
        TournamentPlayer p;

        while (plIterator.hasNext()){

            p = plIterator.next();
            
            if (curPts != p.getCurMatchPts()){
                curPts = p.getCurMatchPts();
                if (curBracketSize%2 == 1){
                    bracket.add(p);
                    brackets.add(bracket);
                    bracket = new ArrayList<TournamentPlayer>();
                    curBracketSize = 0;
                }else{
                    brackets.add(bracket);
                    bracket = new ArrayList<TournamentPlayer>();
                    bracket.add(p);
                    curBracketSize = 1;
                }
                
            }else{
                bracket.add(p);
                curBracketSize++;
            }

        }
    }

    public Standings getStandings(){
        return prevRoundStandings;
    }



}
