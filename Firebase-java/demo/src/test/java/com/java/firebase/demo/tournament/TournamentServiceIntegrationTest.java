package com.java.firebase.demo.tournament;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.java.firebase.demo.tournament.Tournament;
import com.java.firebase.demo.tournament.Match;
import com.java.firebase.demo.user.UserCredentials;

import static org.junit.jupiter.api.Assertions.*;
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


public class TournamentServiceIntegrationTest {
    @LocalServerPort
	private int port;
    
    private final String baseUrl = "http://localhost:";

    @Autowired
    private TestRestTemplate restTemplate;

    private String token;

    @BeforeAll
    public void setup() throws Exception {

        String tournamentName = "Tournament Integration Testing";

        //GET JWT token through logging in

        URI url = new URI(baseUrl + port + "/login");
        UserCredentials userCredentials = new UserCredentials();
        userCredentials.setEmail("admin@gmail.com");
        userCredentials.setPassword("1@Secured");

        ResponseEntity<String> result = restTemplate.postForEntity(url, userCredentials, String.class);
        assertEquals(200, result.getStatusCode().value());
        token = result.getBody();
    }

    @Test
    @Order(1)
    public void testCreateTournament_Success() throws Exception{

        // Create tournament

        URI urlForCreateTournament = new URI(baseUrl + port + "/tournament/create");
        Tournament tournament = new Tournament();

        tournament.setAdminList(new ArrayList<String>());
        tournament.setCurrentRound("0");
        tournament.setRegistrationDeadline("2024-12-08");
        tournament.setStartDate("2024-12-09");
        tournament.setEndDate("2024-12-09");
        tournament.setLocation("One Infinite Loop, Cupertino, CA 95014, USA");
        tournament.setTournamentName("Tournament Integration Testing");
        tournament.setParticipatingPlayers(new ArrayList<>());
        tournament.setNumberOfPlayers(0);
        tournament.setExpectedNumRounds(0);
        tournament.setTournamentDesc("This tournament instance is created for and from integration testing");
        tournament.setInProgress(false);
        tournament.setImageUrl(null); // this should be null first, then in another test we check if the firebase storage call works to amend the actual object
        tournament.setRounds(new ArrayList<>());

        // http request for headers + json
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.set("Content-Type","application/json");

        HttpEntity<Tournament> createTournamentrequest = new HttpEntity<>(tournament, headers);
        ResponseEntity<String> createTournamentResult = restTemplate.postForEntity(urlForCreateTournament, createTournamentrequest, String.class);
        assertEquals(200, createTournamentResult.getStatusCode().value());
    }
  
    @Test
    @Order(2)
    public void createPlayerInTournament_Success() throws Exception{
        String tournamentName = "Tournament Integration Testing";
        String encodedTournamentName = URLEncoder.encode(tournamentName, StandardCharsets.UTF_8.toString());

        // Create players

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<Void> createPlayerHeader = new HttpEntity<>(headers);

        URI urlForCreatePlayer;
        ResponseEntity<String>createPlayerResult;
        for (int i = 1; i <= 8; i++){
            urlForCreatePlayer = new URI(baseUrl + port + "/tournament/player/create?tournamentName="+encodedTournamentName+"&participatingPlayerName=user" + i);
            createPlayerResult = restTemplate.postForEntity(urlForCreatePlayer, createPlayerHeader, String.class);

            assertEquals(200, createPlayerResult.getStatusCode().value());
        }
    } 


    @Test
    @Order(3)
    public void getTournament_Success() throws Exception {
        String tournamentName = "Tournament Integration Testing";
        String encodedTournamentName = URLEncoder.encode(tournamentName, StandardCharsets.UTF_8.toString());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        URI urlForGetTournament = new URI(baseUrl + port + "/tournament/get?tournamentName=" + encodedTournamentName);
        ResponseEntity<Tournament> getTournamentResult = restTemplate.exchange(urlForGetTournament, HttpMethod.GET, requestEntity, Tournament.class);

        assertEquals(200, getTournamentResult.getStatusCode().value());
        assertNotNull(getTournamentResult.getBody());
        assertEquals(tournamentName, getTournamentResult.getBody().getTournamentName());
    }

    @Test
    @Order(4)
    public void createRound_Success() throws Exception {
        String tournamentName = "Tournament Integration Testing";
        String encodedTournamentName = URLEncoder.encode(tournamentName, StandardCharsets.UTF_8.toString());
    
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
    
        Round round = new Round("1");
        round.setMatches(new ArrayList<Match>());
        HttpEntity<Round> requestEntity = new HttpEntity<>(round, headers);
    
        URI urlForCreateRound = new URI(baseUrl + port + "/tournament/round/create?tournamentName=" + encodedTournamentName);
        ResponseEntity<String> createRoundResult = restTemplate.postForEntity(urlForCreateRound, requestEntity, String.class);
    
        assertEquals(200, createRoundResult.getStatusCode().value());
        assertEquals("Round 1, 0 matches, created in Tournament Integration Testing", createRoundResult.getBody());
    }
    @Test
    @Order(5)
    public void getRound_Success() throws Exception {
        String tournamentName = "Tournament Integration Testing";
        String roundName = "1";
        String encodedTournamentName = URLEncoder.encode(tournamentName, StandardCharsets.UTF_8.toString());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        URI urlForGetRound = new URI(baseUrl + port + "/tournament/round/get?tournamentName=" + encodedTournamentName + "&roundName=" + roundName);
        ResponseEntity<Round> getRoundResult = restTemplate.exchange(urlForGetRound, HttpMethod.GET, requestEntity, Round.class);

        assertEquals(200, getRoundResult.getStatusCode().value());
        assertNotNull(getRoundResult.getBody());
        assertEquals(roundName, getRoundResult.getBody().getRoundName());
}

    @Test
    @Order(6)
    public void createMatch_Success() throws Exception {
        String tournamentName = "Tournament Integration Testing";
        String roundName = "1";
        String encodedTournamentName = URLEncoder.encode(tournamentName, StandardCharsets.UTF_8.toString());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        Match match = new Match("Player1", "Player2", null, 0,0, false, false);
        HttpEntity<Match> requestEntity = new HttpEntity<>(match, headers);

        URI urlForCreateMatch = new URI(baseUrl + port + "/tournament/round/match/create?tournamentName=" + encodedTournamentName + "&roundName=" + roundName);
        ResponseEntity<String> createMatchResult = restTemplate.postForEntity(urlForCreateMatch, requestEntity, String.class);

        assertEquals(200, createMatchResult.getStatusCode().value());
        //assertEquals("Match created successfully", createMatchResult.getBody());
    }

    @Test
    @Order(7)
    public void getMatch_Success() throws Exception {
        String tournamentName = "Tournament Integration Testing";
        String roundName = "1";
        String encodedTournamentName = URLEncoder.encode(tournamentName, StandardCharsets.UTF_8.toString());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        URI urlForGetMatch = new URI(baseUrl + port + "/tournament/round/match/get?tournamentName=" + encodedTournamentName + "&roundName=" + roundName + "&player1=Player1&player2=Player2");
        ResponseEntity<Match> getMatchResult = restTemplate.exchange(urlForGetMatch, HttpMethod.GET, requestEntity, Match.class);

        assertEquals(200, getMatchResult.getStatusCode().value());
        assertNotNull(getMatchResult.getBody());
    }

    @Test
    @Order(8)
    public void updateMatch_Success() throws Exception {
        String tournamentName = "Tournament Integration Testing";
        String roundName = "1";
        String encodedTournamentName = URLEncoder.encode(tournamentName, StandardCharsets.UTF_8.toString());
    
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
    
        Match updatedMatch = new Match("Player1", "Player2", "Player1", 2,1, false, false);
        updatedMatch.setWinner("Player1");
        HttpEntity<Match> requestEntity = new HttpEntity<>(updatedMatch, headers);
    
        URI urlForUpdateMatch = new URI(baseUrl + port + "/tournament/round/match/update?tournamentName=" + encodedTournamentName + "&roundName=" + roundName + "&player1=Player1&player2=Player2");
        ResponseEntity<String> updateMatchResult = restTemplate.exchange(urlForUpdateMatch, HttpMethod.PUT, requestEntity, String.class);
    
        assertEquals(200, updateMatchResult.getStatusCode().value());
        assertEquals("Match updated for Player1 vs Player2 in Tournament Integration Testing at 1", updateMatchResult.getBody());
    }

    @Test
    @Order(9)
    public void createStanding_Success() throws Exception {
        String tournamentName = "Tournament Integration Testing";
        String roundName = "1";
        String encodedTournamentName = URLEncoder.encode(tournamentName, StandardCharsets.UTF_8.toString());
    
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
    
        Standing standing = new Standing(1, "Player1", 6, 0.33, 3, 0.66);
        HttpEntity<Standing> requestEntity = new HttpEntity<>(standing, headers);
    
        URI urlForCreateStanding = new URI(baseUrl + port + "/tournament/round/standing/create?tournamentName=" + encodedTournamentName + "&roundName=" + roundName);
        ResponseEntity<String> createStandingResult = restTemplate.postForEntity(urlForCreateStanding, requestEntity, String.class);
    
        assertEquals(200, createStandingResult.getStatusCode().value());
        assertEquals("One standing created in Tournament Integration Testing, 1", createStandingResult.getBody());
    }

    @Test
    @Order(10)
    public void getStanding_Success() throws Exception {
        String tournamentName = "Tournament Integration Testing";
        String roundName = "1";
        String encodedTournamentName = URLEncoder.encode(tournamentName, StandardCharsets.UTF_8.toString());
    
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
    
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
    
        URI urlForGetStanding = new URI(baseUrl + port + "/tournament/round/standing/get?tournamentName=" + encodedTournamentName + "&roundName=" + roundName + "&rank=1");
        ResponseEntity<Standing> getStandingResult = restTemplate.exchange(urlForGetStanding, HttpMethod.GET, requestEntity, Standing.class);
    
        assertEquals(200, getStandingResult.getStatusCode().value());
        assertNotNull(getStandingResult.getBody());
        assertEquals("Player1", getStandingResult.getBody().getPlayerID());
    }

    @Test
    @Order(11)
    public void deleteTournament_Success() throws Exception {
        String tournamentName = "Tournament Integration Testing";
        String encodedTournamentName = URLEncoder.encode(tournamentName, StandardCharsets.UTF_8.toString());
    
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
    
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
    
        URI urlForDeleteTournament = new URI(baseUrl + port + "/tournament/delete?tournamentName=" + encodedTournamentName);
        ResponseEntity<String> deleteTournamentResult = restTemplate.exchange(urlForDeleteTournament, HttpMethod.DELETE, requestEntity, String.class);
    
        assertEquals(200, deleteTournamentResult.getStatusCode().value());
        assertNotNull(deleteTournamentResult.getBody());
        assertEquals("Successfully deleted tournament and all associated documents for Tournament Integration Testing", deleteTournamentResult.getBody());
    }
       
    

}



        // // https requests for just header
        // HttpHeaders headers = new HttpHeaders();
        // headers.set("Authorization", "Bearer " + token);
        // URI urlForStartTournament = new URI(baseUrl + port + "/tournament/start?tournamentName="+tournamentName);

        // HttpEntity<Void> headerEntity = new HttpEntity<>(headers);

        // ResponseEntity<String> startTournamentResult = restTemplate.exchange(urlForStartTournament, HttpMethod.PUT ,headerEntity, String.class);
        // assertEquals(200, startTournamentResult.getStatusCode().value());
