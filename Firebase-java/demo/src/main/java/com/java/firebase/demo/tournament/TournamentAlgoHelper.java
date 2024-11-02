package com.java.firebase.demo.tournament;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreException;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteResult;
import com.google.common.base.Strings;
import com.java.firebase.demo.algo.AlgoMatch;
import com.java.firebase.demo.algo.AlgoRound;
import com.java.firebase.demo.algo.AlgoStandings;
import com.java.firebase.demo.algo.AlgoTournamentPlayer;
import com.java.firebase.demo.tournament.*;

public class TournamentAlgoHelper {

    
    public static AlgoRound parseIntoAlgoRound1( Tournament tournament){

        final int firstRound = 1; 

        ArrayList<AlgoTournamentPlayer> playerObjs = new ArrayList<AlgoTournamentPlayer>();
        
        List<String> players= tournament.getParticipatingPlayers();

        for (String playerName : players){

            playerObjs.add(new AlgoTournamentPlayer(playerName, new ArrayList<AlgoMatch>()));

        }

        return  new AlgoRound(firstRound, playerObjs);

    }

    public static List<Match> parseAlgoRoundMatchesToMatch( AlgoRound roundToParse){

        List<AlgoMatch> matchesToParse = roundToParse.getAlgoMatches();

        List<Match> parsedMatches = new ArrayList<>();

        for (AlgoMatch algoMatch : matchesToParse){
                
            // create new match 

            Match match = new Match();
            String player1Name = algoMatch.getPlayer1().getPlayerID();

            // if is bye

            if (algoMatch.isBye()){
                match.setPlayer1(player1Name);
                match.setBye(true);
                
            }else{

                String player2Name = algoMatch.getPlayer2().getPlayerID();
                match.setPlayer1(player1Name);
                match.setPlayer2(player2Name);
                match.setBye(false);

            }

            parsedMatches.add(match);

        }

        return parsedMatches;

    }


    public static void updateDataBasewithMatches ( Tournament tournament, String roundName, List<Match> matchesToupdate){

        for ( Match match : matchesToupdate){

            String player1Name = match.getPlayer1();

            if (!match.isBye()){

                String player2Name = match.getPlayer2();
                updatePlayerMatch(tournament, player2Name,generateMatchID(tournament, roundName, match));

            }

            updatePlayerMatch(tournament, player1Name,generateMatchID(tournament, roundName, match));
            createMatch(tournament, roundName, match);

        }
    }




    
}
