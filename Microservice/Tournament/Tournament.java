import java.util.ArrayList;

public class Tournament {

    private List<Round> tournamentRounds;
    private List<TournamentPlayer> participatingPlayers;

    public Tournament( List<TournamentPlayer> participatingPlayers){

        this.participatingPlayers = participatingPlayers;

    }

    public void addPlayer( TournamentPlayer player){
        participatingPlayers.add(player);
    }

    public void nextRound(){
        
    }

    public void beginTournament(){

    }

    
}
