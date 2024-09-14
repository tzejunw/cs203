import java.util.List;

public class Round {
    private List<Match> matches;
    private Standings standingsRound;
    private int roundNumber;
    private List<TournamentPlayer> players;

    public Round(int roundNumber, List<TournamentPlayer> tournamentPlayers){
        players = tournamentPlayers
    }


    public void generateMatches(Standings standing){

    }

    public Standings generateStandings(){
        standingsRound = new Standings(players);
    }

    public Bracket createBracket(){
        
    }

}
