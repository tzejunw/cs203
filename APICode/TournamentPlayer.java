import java.util.ArrayList;
import java.util.List;

public class TournamentPlayer{

    private String playerId;
    private List<Match> pastMatches;
    private User playerInfo;
    private Boolean isPlaying;

    private int curMatchPts;
    private double curOMW;
    private int curGamePts;
    private double curOGW;
    

    private final int matchWinPts = 3;
    private final int gameWinPts = 3;
    private final int drawPts = 1;



    public TournamentPlayer( User playerInfo, List<Match> matches){
        pastMatches = matches;
        this.playerInfo = playerInfo;
    }

    public String getName(){

    }

    public void addMatch( Match match){
        pastMatches.add(match);
    }

    public int getTotalMatchPoints(){

        //return pastMatches.stream().filter(m -> (getP1().equals(this) && isP1Winner) && (getP2().equals(this) && !isP1Winner)).map(m :: getGameWins).stream().reduce(0, (a, b) -> a + b, Integer::sum);

        int matchWins = 0;

        for ( Match m : pastMatches){
            if (m.getP1().equals(this) && m.isP1Winner() || m.getP2().equals(this) && !m.isP1Winner()){
                matchWins++;
            }
        }

        return matchWins;
    }

    public int getTotalGamePoints(){


        int gameWins = 0;

        for ( Match m: pastMatches){
            if (m.getP1().equals(this) && m.isP1Winner() || m.getP2().equals(this) && !m.isP1Winner()){
                gameWins += m.getGameWins();
            }else{
                gameWins +=  m.getGameLosses();
            }
        }

        return gameWins;
    }

    public int getDraws(){

        int draws = 0;

        for ( Match m: pastMatches){
            if (m.isDraw()){
                draws++;
            }
        }
        return draws;
    }

    public int roundsPlayed(){
        return pastMatches.size() - getByes();
    }

    public double getOMW(){

        double OMW = 0;

        for ( Match m : pastMatches){
            if (!m.isBye()){
                TournamentPlayer opp = m.getP1().equals(this) ? m.getP2() : m.getP1();
                OMW += opp.getTotalMatchWins() / opp.roundsPlayed();    
            }
        }

        return OMW;
    }

    


    



    

    

}
