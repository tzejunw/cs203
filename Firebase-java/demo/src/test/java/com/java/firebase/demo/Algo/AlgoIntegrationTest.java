package com.java.firebase.demo.Algo;

import java.net.URI;

import java.util.*;

import com.java.firebase.demo.tournament.Tournament;
import com.java.firebase.demo.tournament.Match;
import com.java.firebase.demo.user.UserCredentials;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.matches;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.event.annotation.AfterTestClass;


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

public class AlgoIntegrationTest {

    @LocalServerPort
	private int port;
    
    private final String baseUrl = "http://localhost:";

    @Autowired
    private TestRestTemplate restTemplate;

    private String token;
    private String uid;
    private String tournamentName = "iantest4";
    private int numberOfPlayers = 9;

    @BeforeAll
    public void setup() throws Exception {

        //GET JWT token through logging in

        URI url = new URI(baseUrl + port + "/login");
        UserCredentials userCredentials = new UserCredentials();
        userCredentials.setEmail("admin@gmail.com");
        userCredentials.setPassword("1@Secured");

        ResponseEntity<String> result = restTemplate.postForEntity(url, userCredentials, String.class);
        assertEquals(200, result.getStatusCode().value());
        token = result.getBody();
    
 
        // Create tournament

        URI urlForCreateTournament = new URI(baseUrl + port + "/tournament/create");
        Tournament tournament = new Tournament();

        tournament.setAdminList(new ArrayList<String>());
        tournament.setCurrentRound("0");
        tournament.setRegistrationDeadline("2024-12-08");
        tournament.setStartDate("2024-12-09");
        tournament.setEndDate("2024-12-09");
        tournament.setLocation("Singapore");
        tournament.setTournamentName(tournamentName);
        tournament.setParticipatingPlayers(new ArrayList<>());
        tournament.setNumberOfPlayers(0);
        tournament.setExpectedNumRounds(0);
        tournament.setTournamentDesc("test");
        tournament.setInProgress(false);
        tournament.setImageUrl(null);
        tournament.setRounds(new ArrayList<>());


        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.set("Content-Type","application/json");

        HttpEntity<Tournament> createTournamentrequest = new HttpEntity<>(tournament, headers);

        ResponseEntity<String> createTournamentResult = restTemplate.postForEntity(urlForCreateTournament, createTournamentrequest, String.class);
        assertEquals(200, createTournamentResult.getStatusCode().value());
        
        // Create players

        HttpHeaders headersForCreatePlayer = new HttpHeaders();
        headersForCreatePlayer.set("Authorization", "Bearer " + token);
        HttpEntity<Void> createPlayerHeader = new HttpEntity<>(headers);

        URI urlForCreatePlayer;
        ResponseEntity<String>createPlayerResult;
        for (int i = 1; i <= numberOfPlayers; i++){
            urlForCreatePlayer = new URI(baseUrl + port + "/tournament/player/create?tournamentName="+tournamentName+"&participatingPlayerName=user" + i);
            createPlayerResult = restTemplate.postForEntity(urlForCreatePlayer, createPlayerHeader, String.class);

            assertEquals(200, createPlayerResult.getStatusCode().value());
        }

    } 

    @Test
    @Order(1)
    public void testStartTournament_Success() throws Exception{
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        URI urlForStartTournament = new URI(baseUrl + port + "/tournament/start?tournamentName="+tournamentName);

        HttpEntity<Void> headerEntity = new HttpEntity<>(headers);

        ResponseEntity<String> startTournamentResult = restTemplate.postForEntity(urlForStartTournament, headerEntity, String.class);
        assertEquals(200, startTournamentResult.getStatusCode().value());
    }

    @Test
    @Order(2)
    public void testUpdateMatches_Success() throws Exception{
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        URI urlGetMatch;

        //update player matches

        for (int i = 1; i <= numberOfPlayers; i++){

            urlGetMatch = new URI(baseUrl + port + "/tournament/round/match/player/get?tournamentName=" + tournamentName + "&roundName=1&player=user"+i);

            ResponseEntity<Match> matchResponse = restTemplate.exchange(urlGetMatch,HttpMethod.GET,request, Match.class);
            assertEquals(200, matchResponse.getStatusCode().value());
            Match matchToUpdate = matchResponse.getBody();

            String player1 = matchToUpdate.getPlayer1();
            String player2 = matchToUpdate.getPlayer2();

            matchToUpdate.setWinner(player1);
            matchToUpdate.setWins(2);

            URI urlUpdateMatch = new URI(baseUrl + port + "/tournament/round/match/update?tournamentName=" + tournamentName + "&roundName=1&player1=" + player1 + "&player2=" + player2);
            HttpHeaders headersForUpdating = new HttpHeaders();
            headersForUpdating.set("Authorization", "Bearer " + token);
            headersForUpdating.set("Content-Type","application/json");

            HttpEntity<Match> updateMatchRequest = new HttpEntity<>(matchToUpdate, headers);
            ResponseEntity<String> updateMatchResult = restTemplate.exchange(urlUpdateMatch, HttpMethod.PUT, updateMatchRequest, String.class);
            assertEquals(200, updateMatchResult.getStatusCode().value());
            
        }

    }

    @Test
    @Order(3)
    public void testEndRound_Success() throws Exception{
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> headerEntity = new HttpEntity<>(headers);

        URI urlEndRound = new URI(baseUrl + port + "/tournament/round/end?tournamentName=" + tournamentName + "&roundName=1");

        ResponseEntity<String> endRoundResult = restTemplate.exchange(urlEndRound, HttpMethod.GET ,headerEntity, String.class);
        assertEquals(200, endRoundResult.getStatusCode().value());

    }
    @Test
    @Order(4)
    public void testBeginRound_Sucess() throws Exception{

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> headerEntity = new HttpEntity<>(headers);

        URI urlStartRound = new URI(baseUrl + port + "/tournament/round/start?tournamentName=" + tournamentName);

        ResponseEntity<String> startRoundResult = restTemplate.exchange(urlStartRound, HttpMethod.GET,headerEntity, String.class);
        assertEquals(200, startRoundResult.getStatusCode().value());
        
    }/* 

    @Test
    @Order(5)
    public void testUpdateMatches_Success2() throws Exception{
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        URI urlGetMatch;

        //update player matches

        for (int i = 1; i <= numberOfPlayers; i++){

            urlGetMatch = new URI(baseUrl + port + "/tournament/round/match/player/get?tournamentName=" + tournamentName + "&roundName=2&player=user"+i);

            ResponseEntity<Match> matchResponse = restTemplate.exchange(urlGetMatch,HttpMethod.GET,request, Match.class);
            assertEquals(200, matchResponse.getStatusCode().value());
            Match matchToUpdate = matchResponse.getBody();

            String player1 = matchToUpdate.getPlayer1();
            String player2 = matchToUpdate.getPlayer2();

            matchToUpdate.setWinner(player1);
            matchToUpdate.setWins(2);

            URI urlUpdateMatch = new URI(baseUrl + port + "/tournament/round/match/update?tournamentName=" + tournamentName + "&roundName=2&player1=" + player1 + "&player2=" + player2);
            HttpHeaders headersForUpdating = new HttpHeaders();
            headersForUpdating.set("Authorization", "Bearer " + token);
            headersForUpdating.set("Content-Type","application/json");

            HttpEntity<Match> updateMatchRequest = new HttpEntity<>(matchToUpdate, headers);
            ResponseEntity<String> updateMatchResult = restTemplate.exchange(urlUpdateMatch, HttpMethod.PUT, updateMatchRequest, String.class);
            assertEquals(200, updateMatchResult.getStatusCode().value());
            
        }

    }
    @Test
    @Order(6)
    public void testEndRound_Success2() throws Exception{
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> headerEntity = new HttpEntity<>(headers);

        URI urlEndRound = new URI(baseUrl + port + "/tournament/round/end?tournamentName=" + tournamentName + "&roundName=2");

        ResponseEntity<String> endRoundResult = restTemplate.exchange(urlEndRound, HttpMethod.GET ,headerEntity, String.class);
        assertEquals(200, endRoundResult.getStatusCode().value());

    }

    @Test
    @Order(7)
    public void testBeginRound_Sucess2() throws Exception{

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> headerEntity = new HttpEntity<>(headers);

        URI urlStartRound = new URI(baseUrl + port + "/tournament/round/start?tournamentName=" + tournamentName);

        ResponseEntity<String> startRoundResult = restTemplate.exchange(urlStartRound, HttpMethod.GET,headerEntity, String.class);
        assertEquals(200, startRoundResult.getStatusCode().value());
        
    }

    @Test
    @Order(8)
    public void testUpdateMatches_Success3() throws Exception{
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        URI urlGetMatch;

        //update player matches

        for (int i = 1; i <= numberOfPlayers; i++){

            urlGetMatch = new URI(baseUrl + port + "/tournament/round/match/player/get?tournamentName=" + tournamentName + "&roundName=3&player=user"+i);

            ResponseEntity<Match> matchResponse = restTemplate.exchange(urlGetMatch,HttpMethod.GET,request, Match.class);
            assertEquals(200, matchResponse.getStatusCode().value());
            Match matchToUpdate = matchResponse.getBody();

            String player1 = matchToUpdate.getPlayer1();
            String player2 = matchToUpdate.getPlayer2();

            matchToUpdate.setWinner(player1);
            matchToUpdate.setWins(2);

            URI urlUpdateMatch = new URI(baseUrl + port + "/tournament/round/match/update?tournamentName=" + tournamentName + "&roundName=3&player1=" + player1 + "&player2=" + player2);
            HttpHeaders headersForUpdating = new HttpHeaders();
            headersForUpdating.set("Authorization", "Bearer " + token);
            headersForUpdating.set("Content-Type","application/json");

            HttpEntity<Match> updateMatchRequest = new HttpEntity<>(matchToUpdate, headers);
            ResponseEntity<String> updateMatchResult = restTemplate.exchange(urlUpdateMatch, HttpMethod.PUT, updateMatchRequest, String.class);
            assertEquals(200, updateMatchResult.getStatusCode().value());
            
        }

    }
/* 
    @AfterAll
    public void takeDown() throws Exception {
        URI uri = new URI(baseUrl + port + "/tournament/delete?tournamentName=" + tournamentName);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> result = restTemplate.exchange(uri,HttpMethod.DELETE ,request, String.class);
        assertEquals(200, result.getStatusCode().value());
    } */ 

}
