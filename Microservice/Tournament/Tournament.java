import java.util.ArrayList;
import java.util.*;

public class Tournament {

    private ArrayList<Round> tournamentRounds;
    private ArrayList<TournamentPlayer> participatingPlayers;
    private int curRoundNumber;
    private int totalRounds;

    public Tournament( ArrayList<TournamentPlayer> participatingPlayers, int totalRounds){

        this.participatingPlayers = participatingPlayers;
        this.totalRounds = totalRounds;
        curRoundNumber = 1;

    }

    public void addPlayer( TournamentPlayer player){
        participatingPlayers.add(player);
    }

    public int curRoundNumber(){
        return curRoundNumber;
    }

    public void nextRound(){
        tournamentRounds.get(curRoundNumber).generateMatches();

    }

    public void endRound(){
        curRoundNumber++;
        Round nextRound = new Round(curRoundNumber, participatingPlayers);
        nextRound.generateStandings();
        tournamentRounds.add(nextRound);

    }


    public void beginTournament(){
        Round firstRound = new Round(curRoundNumber, participatingPlayers);
        firstRound.generateRoundOne();
        tournamentRounds.add(firstRound);
    }

    
}
