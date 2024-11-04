package com.java.firebase.demo.Algo;

import java.util.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.java.firebase.demo.algo.AlgoMatch;
import com.java.firebase.demo.algo.AlgoRound;
import com.java.firebase.demo.algo.AlgoStandings;
import com.java.firebase.demo.algo.AlgoTournamentPlayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.matches;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.Spy;

@ExtendWith(MockitoExtension.class)


public class AlgoTest {

    AlgoRound testRound;
    ArrayList<AlgoTournamentPlayer> players;
    List<AlgoMatch> matches;
    

    @BeforeEach
    void setup(){

        // Generates 35 players and creates first round with match results updated
        
        players = new ArrayList<AlgoTournamentPlayer>();

        for ( int i = 1; i <= 35; i++){
            players.add(new AlgoTournamentPlayer("" + i, new ArrayList<>()));
        }

        testRound = new AlgoRound(1, players);

        testRound.generateRoundOne();
        
        List<AlgoMatch> matches = testRound.getAlgoMatches();

        for ( AlgoMatch match : matches){
            match.update(match.getPlayer1(), 2, 0);
        }

        

    }

    void completeRounds(int roundsToComplete){

        //simulates round finishing and generating the new round with match results updated

        for ( int i = 0; i < roundsToComplete; i++){

            testRound =  new AlgoRound(0, players);
            testRound.generateStandings();
            testRound.generateAlgoMatches();

            matches =  testRound.getAlgoMatches();

            for ( AlgoMatch match : matches){
                match.update(match.getPlayer2(), 2, 0);
            }

        }

    }

   
    @Test
    void generateRound_OddNumberOfPlayers_ReturnsMatchesWithOneOfThemABye(){

        int byeCount = 0;

        for (AlgoMatch match : testRound.getAlgoMatches()){
            if (match.isBye()){
                byeCount++;
            }
        }
        assertEquals( 1, byeCount);
    }

    @Test
    void generateRound_OddNumberOfPlayersinTwoBrackets_ReturnsTwoDownPair(){

        completeRounds(2);

        matches =  testRound.getAlgoMatches();

        int downpairs = 0;
        
        // check for downpair match and count number

        for (AlgoMatch match : matches){
            if (!match.isBye()){
                downpairs += match.getPlayer1().getCurMatchPts() != match.getPlayer2().getCurMatchPts() ? 1 : 0; 
            }
        }
        assertEquals(2, downpairs);

    }

    @Test 
    void generateStandings_inSortedOrder_ReturnsSortedOrder(){

        completeRounds(2);

        boolean isSorted = true;

         List<AlgoTournamentPlayer> standings = testRound.getStandings().getStandings();

         AlgoTournamentPlayer prevPlayer = standings.get(0);

        for ( AlgoTournamentPlayer player : standings){
            if ( player.getCurMatchPts() > prevPlayer.getCurMatchPts()){
                isSorted = false;
                break;
            }
            prevPlayer = player;
        }


        assertEquals(true, isSorted);
    }

    @Test
    void generateRound1_GeneratesCorrectNumberOfMatches_ReturnsCorrectNumber(){
        int numberOfMatches = 18;
        matches =  testRound.getAlgoMatches();

        assertEquals(numberOfMatches, matches.size());
    }

    @Test
    void generateRound_GeneratesCorrectNumberOfMatches_ReturnsCorrectNumber(){
        completeRounds(1);
        int numberOfMatches = 18;
        matches =  testRound.getAlgoMatches();
        assertEquals(numberOfMatches, matches.size());
    }


    @Test
    void getCurOMW_OMWinCorrectRange_ReturnsBelow1(){

        boolean allWithinRange = true;
        
        for (AlgoTournamentPlayer player : players){
            if (player.getOMW() > 1.0 && player.getOMW() < 0.33){
                allWithinRange = false;
                break;
            }
        }

        assertEquals(true, allWithinRange);
    }

    @Test
    void getCurOGW_OGWinCorrectRange_ReturnsBelow1(){

        boolean allWithinRange = true;
        
        for (AlgoTournamentPlayer player : players){
            if (player.getOGW() > 1.0 && player.getOGW() < 0.33){
                allWithinRange = false;
                break;
            }
        }

        assertEquals(true, allWithinRange);
    }

    @Test
    void getCurGW_CorrectRange_ReturnsBelow1(){

        boolean allWithinRange = true;
        
        for (AlgoTournamentPlayer player : players){
            if (player.getCurGW() > 1.0){
                allWithinRange = false;
                break;
            }
        }

        assertEquals(true, allWithinRange);

    }

    @Test 
    void getCurMatchPts_CorrectGamePointsForCorrespondingWins(){

        completeRounds(1);

        matches =  testRound.getAlgoMatches();

        boolean scoreCorrespondsWithWins = true;
        
        for (AlgoMatch match : matches){

            if (!match.isBye()){
                
                AlgoTournamentPlayer player1 = match.getPlayer1();
                AlgoTournamentPlayer player2 = match.getPlayer2();
                
                if (player2.getCurMatchPts() != 3 && player1.getCurMatchPts() != 0){
                    scoreCorrespondsWithWins = false;
                    break;
                }
            }else{
                if (match.getWinner().getCurMatchPts() != 3){
                    scoreCorrespondsWithWins =  false;
                    break;
                }
            }
            
        }
        assertEquals(true, scoreCorrespondsWithWins);
    }

    @Test
    void createBrackets_BracketNumbersAreCorrect_returns4(){
        
        completeRounds(2);

        // 4 brackets after 3 rounds, 0-3, 1-2, 2-1, 3-0

        ArrayList<List<AlgoTournamentPlayer>> brackets = testRound.getBracket();

        assertEquals(4, brackets.size());

    }

    
    
    
}
