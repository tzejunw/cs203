import java.util.*;

public class Standings {
    
    ArrayList<TournamentPlayer> TournamentPlayers;

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
