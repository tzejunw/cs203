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

        HashSet<String> nodups = new HashSet<String>();

        ArrayList<TournamentPlayer> players = new ArrayList<TournamentPlayer>();
        

        records.remove(0);

        // for (List<String> i : records){

        //     if (!nodups.contains(i.get(0)) && !nodups.contains(i.get(2))){
        //         System.out.println(i.get(0)+","+i.get(2)+","+i.get(3));

        //     }

            
        //     nodups.add(i.get(0));
        //     nodups.add(i.get(2));
        // }



 
        for (List<String> i : records){

            TournamentPlayer p1 = new TournamentPlayer(i.get(0), new ArrayList<Match>());
            TournamentPlayer p2 = new TournamentPlayer(i.get(2), new ArrayList<Match>());
            
            Match m = null;
            
            if (!nodups.contains(p1.getPlayerID()) && !nodups.contains(p2.getPlayerID())){
                m = new Match(p1, p2);
                nodups.add(p2.getPlayerID());
                nodups.add(p1.getPlayerID());

                players.add(p1);
                players.add(p2);

                if (i.get(3).charAt(0)== '2'){
                    m.update(p1, 2, (int)(i.get(3).charAt(2))- (int)'0');
                }
                else if(i.get(3).charAt(2) == '2'){
                    m.update(p2, 2, (int)(i.get(3).charAt(0))- (int)'0');
                }
                testMatches.add(m);

            }
            
        }

        for (String id : nodups){
            System.out.println(id);
        }

        // for ( Match m : testMatches){
        //     System.out.println( m.getPlayer1().getPlayerID() +","+ m.getPlayer2().getPlayerID()  +","+ m.getWinner().getPlayerID() +","+m.getGameWins()  +","+ m.getGameLosses());
        // }

        /* 
        TournamentPlayer bye = new TournamentPlayer("byeman", new ArrayList<Match>());
        bye.addMatch(new Match(bye));
        players.add(bye);
        System.out.println(nodups.size());
        System.out.println("total players: " + players.size());

        
        
        

        Round testRound = new Round(1, players);

        Standings s = testRound.generateStandings();

        // for ( TournamentPlayer p : s.getSortedPlayers()){
        //     System.out.println(p.getPlayerID() + " - " + p.getCurMatchPts());
        // }

        testRound.generateMatches(new ArrayList<Match>());

        System.out.println("b4 " + testRound.getRoundMatches().size());

        for ( Match m : testRound.getRoundMatches()){
            //System.out.println(m.getPlayer1().getPlayerID() + " " + m.getPlayer1().getCurMatchPts() + " vs " + m.getPlayer2().getPlayerID()+ " " + m.getPlayer2().getCurMatchPts());
            m.update(m.getPlayer1(), 2, 0);
        }

        

        // for ( TournamentPlayer p : s.getSortedPlayers()){
        //     System.out.println(p.getPlayerID() + " - " + p.getCurMatchPts());
        // }

        for ( int i =0 ; i <4; i++){
        s = testRound.generateStandings();
        testRound.generateMatches(new ArrayList<Match>());

        for ( Match m : testRound.getRoundMatches()){
            // System.out.println(m.getPlayer1().getPlayerID() + " " + m.getPlayer1().getCurMatchPts() + " vs " + m.getPlayer2().getPlayerID()+ " " + m.getPlayer2().getCurMatchPts());
           m.update(m.getPlayer1(), 2, 0);
        }
        }

        System.out.println("after " + testRound.getRoundMatches().size());

        int count = 0;

        System.out.println();

        ArrayList<Integer> l = new ArrayList<Integer>();
        for ( Match m : testRound.getRoundMatches()){

            if (m.isBye()){
                System.out.println(m.getPlayer1().getPlayerID() + " is bye");
            }else{
                System.out.println(m.getPlayer1().getPlayerID() + " " + m.getPlayer1().getCurMatchPts() + " vs " + m.getPlayer2().getPlayerID()+ " " + m.getPlayer2().getCurMatchPts());
                count += m.getPlayer1().getCurMatchPts() != m.getPlayer2().getCurMatchPts() ? 1 : 0; 
                if (m.getPlayer1().getCurMatchPts() != m.getPlayer2().getCurMatchPts()) {
                    l.add(m.getPlayer1().getCurMatchPts());
                    l.add(m.getPlayer2().getCurMatchPts());
                }
            }
            
            //m.update(m.getPlayer1(), 2, 0);


            
        }

        System.out.println();

        for (List<TournamentPlayer> b : testRound.getBracket()){
            if (b != null && b.size() > 0){
                System.out.println(b.get(0).getCurMatchPts() + " : " + b.size());
            }
            
        }

        System.out.println("total downpairs: " + count);

        System.out.println(l);






*/

    }
}
