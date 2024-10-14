import java.util.*;
import java.io.*;

public class Testing {


    public static void main(String[] args) {

        
        // TournamentPlayer testplayer = new TournamentPlayer("1", new ArrayList<Match>());
        // TournamentPlayer testplayer2 = new TournamentPlayer("2", new ArrayList<Match>());
        // TournamentPlayer testplayer3 = new TournamentPlayer("3", new ArrayList<Match>());

        // Match m1 = new Match(testplayer,testplayer2);
        // Match m2 = new Match(testplayer2,testplayer3);
        // Match m3 = new Match(testplayer3,testplayer);

        // m1.update(testplayer, 2, 0);
        // m2.update(testplayer2, 2, 1);
        // m3.update(testplayer, 2,0);

        // testplayer.addMatch(m1);
        // testplayer2.addMatch(m1);
        // testplayer.addMatch(m3);
        // testplayer2.addMatch(m2);
        // testplayer3.addMatch(m3);
        // testplayer3.addMatch(m2);

        // testplayer.updateCurPoints();
        // testplayer2.updateCurPoints();
        // testplayer3.updateCurPoints();

        // System.out.println(testplayer.getCurGW());
        // System.out.println(testplayer.getCurMatchPts());
        // System.out.println(testplayer.getOGW());

        List<List<String>> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("testdata.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                records.add(Arrays.asList(values));
            }
        }catch (Exception E){

        }

        ArrayList<Match> testMatches = new ArrayList<Match>();

        HashSet<TournamentPlayer> nodups = new HashSet<TournamentPlayer>();

        records.remove(0);
        
        for (List<String> i : records){


            TournamentPlayer p1 = new TournamentPlayer(i.get(0), new ArrayList<Match>());
            TournamentPlayer p2 = new TournamentPlayer(i.get(2), new ArrayList<Match>());
            
            Match m = null;
            
            if (!nodups.contains(p1)){
                m = new Match(p1, p2);
                nodups.add(p2);
                nodups.add(p1);
                p1.addMatch(m);
                p2.addMatch(m);
            }
            

            if (i.get(3).charAt(0)== '2'){
                m.update(p1, 2, (int)(i.get(3).charAt(2))- (int)'0');
            }
            if(i.get(3).charAt(2) == '2'){
                m.update(p2, 2, (int)(i.get(3).charAt(0))- (int)'0');
            }
            testMatches.add(m);

        }


        ArrayList<TournamentPlayer> players = new ArrayList<TournamentPlayer>();
        players.addAll(nodups);

        Round testRound = new Round(2, players);

        int winners = 0;
        for ( TournamentPlayer p : testRound.generateStandings().getSortedPlayers()){
            //System.out.println(p.getUserID() +" " + p.getCurMatchPts());
            winners += p.getCurMatchPts()/3;
        }
        //System.out.println(winners);

        testRound.generateStandings();
        testRound.generateMatches(new ArrayList<Match>());

        for (Match m : testRound.getRoundMatches()){
            m.update(m.getP1(), 2, 0);
            m.getP1().addMatch(m);
            m.getP2().addMatch(m);
            
        }

        testRound.generateStandings();
        testRound.generateMatches(new ArrayList<Match>());

        

        System.out.println(testRound.getRoundMatches().size());

        for (Match m : testRound.getRoundMatches()){
            System.out.println(m.getP1().getUserID() +" vs " + m.getP2().getUserID());
            System.out.println(m.getP1().getTotalMatchPoints() + " - " + m.getP2().getTotalMatchPoints());
        }





        


    }
}
