package com.java.firebase.demo.tournament;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

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

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.validation.metadata.ExecutableDescriptor;

@Service
public class TournamentService {
    private final Firestore firestore;
    private final TournamentValidator tournamentValidator;

    public TournamentService(Firestore firestore, TournamentValidator tournamentValidator) {
        this.firestore = firestore;
        this.tournamentValidator = tournamentValidator;
    }


    public String createPlayer(String tournamentName, String participatingPlayerName) throws InterruptedException, ExecutionException {
        DocumentReference playerDocRef = firestore.collection("tournament")
                                                  .document(tournamentName)
                                                  .collection("participatingPlayers")
                                                  .document(participatingPlayerName); // Concatenated Id
        
        ApiFuture<WriteResult> playerResult = playerDocRef.set(new HashMap<>()); // Creating with an empty map
        playerResult.get(); 
    
        // Add a string to represent the past match

        Map<String, Object> data = new HashMap<>();
        data.put("pastMatches", new ArrayList<>()); // Empty array for pastMatches
    
        ApiFuture<WriteResult> updateResult = playerDocRef.set(data);

        // Update the pastMatches array field in the document
        //ApiFuture<WriteResult> updateResult = playerDocRef.update("pastMatches", FieldValue.arrayUnion());
    
        // Wait for the update to complete
        updateResult.get();
    
        return "Player with past matches added successfully";
    }

    public String updatePlayerMatch(String tournamentName, String participatingPlayerName, String matchId) throws InterruptedException, ExecutionException {
        // Get the reference to the participatingPlayer's document
        DocumentReference playerDocRef = firestore.collection("tournament")
                                                  .document(tournamentName)
                                                  .collection("participatingPlayers")
                                                  .document(participatingPlayerName);
    
        // Use arrayUnion to add the matchId to the pastMatches array field
        ApiFuture<WriteResult> updateResult = playerDocRef.update("pastMatches", FieldValue.arrayUnion(matchId));
    
        // Wait for the update to complete
        updateResult.get();
    
        return "Match ID added successfully to pastMatches";
    }

    public ParticipatingPlayer getPlayer(String tournamentName, String participatingPlayerName) throws InterruptedException, ExecutionException {
        // Reference to the player's document in the participatingPlayers subcollection
        DocumentReference playerDocRef = firestore.collection("tournament")
                                                .document(tournamentName)
                                                .collection("participatingPlayers")
                                                .document(participatingPlayerName);

        // Get the document snapshot (representing the player's data)
        DocumentSnapshot document = playerDocRef.get().get();
        ParticipatingPlayer participatingPlayer;
        // Check if the document exists
        if (document.exists()) {
            // Return the document data as a JSON string or formatted output
            System.out.println("Player document found");
            participatingPlayer = document.toObject(ParticipatingPlayer.class); // Can be converted to JSON if required
        } else {
            System.out.println("player not found");
            participatingPlayer = new ParticipatingPlayer();
            participatingPlayer.setPastMatches(new ArrayList<>());
        }
        return participatingPlayer;
    }

    public List<String> getAllPlayer(String tournamentName) throws ExecutionException, InterruptedException {
        // Retrieve all documents from the "tournament" collection
        ApiFuture<QuerySnapshot> future = firestore.collection("tournament")
                                                    .document(tournamentName)
                                                    .collection("participatingPlayers")
                                                    .get();
        
        // QuerySnapshot contains all documents in the collection
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        
        // Create a list to hold the Tournament objects
        List<String> players = new ArrayList<>();
        
        // Convert each document to a Tournament object and add it to the list
        for (DocumentSnapshot document : documents) {
            String playerId = document.getId();
            System.out.println(playerId);
            if (!playerId.equals("emptyPlayerDoc")) {
                players.add(playerId);
            }

        }
        
        return players;
    }

    public List<Match> getPlayerPastMatches(String tournamentName, String participatingPlayerName) throws InterruptedException, ExecutionException {
        ParticipatingPlayer player = getPlayer(tournamentName, participatingPlayerName);
        List<String> pastMatchIds = player.getPastMatches();
        List<Match> pastMatches = new ArrayList<>();
        for (String matchId : pastMatchIds) {
            Map<String, String> matchMap = splitMatchId(matchId);
            Match match = getMatch(tournamentName, matchMap.get("roundName"), matchMap.get("player1"), matchMap.get("player2"));
            pastMatches.add(match);
        }
        return pastMatches;
    }

    public String deletePlayer(String tournamentName, String participatingPlayerName) throws InterruptedException, ExecutionException {
        try {
            // Step 1: Delete the player document from the "participatingPlayer" subcollection
            ApiFuture<WriteResult> deleteFuture = firestore
                    .collection("tournament")
                    .document(tournamentName)
                    .collection("participatingPlayers")
                    .document(participatingPlayerName)
                    .delete();
            
            // Wait for the delete operation to complete
            deleteFuture.get();

            // Step 2: Remove the player from the "participatingPlayers" array in the tournament document
            DocumentReference docRef = firestore.collection("tournament").document(tournamentName);
            
            ApiFuture<WriteResult> updateFuture = docRef.update("participatingPlayers", FieldValue.arrayRemove(participatingPlayerName));

            // Wait for the update operation to complete
            updateFuture.get();

            // Return success message if both operations succeed
            return participatingPlayerName + " deleted from " + tournamentName;

        } catch (Exception e) {
            // Handle any errors that occur during deletion or update
            return "Error deleting " + participatingPlayerName + " from " + tournamentName + ": " + e.getMessage();
        }
    }

    public String endTournament(String tournamentName) throws InterruptedException, ExecutionException {
        // Get the reference to the participatingPlayer's document
        DocumentReference tournamentDocRef = firestore.collection("tournament")
                                                .document(tournamentName);

        // Use arrayUnion to add the matchId to the pastMatches array field
        ApiFuture<WriteResult> updateResult = tournamentDocRef.update("inProgress", false);

        // Wait for the update to complete
        updateResult.get();

        return "Tournament ended";
    }
        // CRUD for Tournaments
    // This takes one of the specified json fields, here .getTournamentName(),  and sets it as the documentId (document identifier)
    // if you want firebase to generate documentId for us, leave .document() blank
    // generates a tournament with an empty Round 1
    public String createTournament(Tournament tournament) throws ExecutionException, InterruptedException {
        if(tournamentValidator.isTournamentValid(tournament)) {
            System.out.println("Valid Input"); // validations, will throw error if not valid
        }
        if (!tournamentValidator.isNameUnique(tournament.getTournamentName())){
            throw new IllegalArgumentException("The same tournament name already exists.");
        }
        
        // Add the tournament object as a document in Firestore
        ApiFuture<WriteResult> collectionsApiFuture = firestore.collection("tournament")
                                                                  .document(tournament.getTournamentName())
                                                                  .set(tournament);
        
        // Wait for the tournament document to be created
        collectionsApiFuture.get();

        
        return collectionsApiFuture.get().getUpdateTime().toString();
    }
    // For this Firebase doc, the tournamentName is the documentId.
    // The key in the GET request must be "documentid", and the value is the tournamentName
    public Tournament getTournament(String tournamentName) throws ExecutionException, InterruptedException {
        Tournament tournament = fetchTournament(tournamentName);
        if (tournament != null) {
            List<Round> rounds = fetchRounds(tournamentName);
            tournament.setRounds(rounds);
    
            List<String> players = fetchParticipatingPlayers(tournamentName);
            tournament.setParticipatingPlayers(players);
        }
        return tournament;
    }
    
    public Tournament fetchTournament(String tournamentName) throws ExecutionException, InterruptedException {
        DocumentReference documentReference = firestore.collection("tournament").document(tournamentName);
        ApiFuture<DocumentSnapshot> future = documentReference.get();
        DocumentSnapshot document = future.get();
    
        if (document.exists()) {
            return document.toObject(Tournament.class);
        }
        return null;
    }
    
    public List<Round> fetchRounds(String tournamentName) throws ExecutionException, InterruptedException {

        QuerySnapshot roundsSnapshot = firestore.collection("tournament").document(tournamentName).collection("round").get().get();

        // DocumentReference documentReference = firestore.collection("tournament").document(tournamentName);
        // CollectionReference roundsCollection = documentReference.collection("round");
        // ApiFuture<QuerySnapshot> roundsFuture = roundsCollection.get();
        // QuerySnapshot roundsSnapshot = roundsFuture.get();
    
        List<Round> rounds = new ArrayList<>();
        if (!roundsSnapshot.isEmpty()) {
            System.out.println("Found " + roundsSnapshot.size() + " round documents");
            List<String> roundIds = roundsSnapshot.getDocuments().stream()
                .map(DocumentSnapshot::getId)
                .collect(Collectors.toList());
    
            for (String roundId : roundIds) {
                Round round = getRound(tournamentName, roundId); // You may need to adjust this
                rounds.add(round);
            }
        } else {
            System.out.println("No round documents found");
        }
        return rounds;
    }
    
    public List<String> fetchParticipatingPlayers(String tournamentName) throws ExecutionException, InterruptedException {
        DocumentReference documentReference = firestore.collection("tournament").document(tournamentName);
        CollectionReference playerCollection = documentReference.collection("participatingPlayers");
        ApiFuture<QuerySnapshot> playerFuture = playerCollection.get();
        QuerySnapshot playerSnapshot = playerFuture.get();
    
        List<String> players = new ArrayList<>();
        if (!playerSnapshot.isEmpty()) {
            System.out.println("Found " + playerSnapshot.size() + " player documents");
            List<String> playerIds = playerSnapshot.getDocuments().stream()
                .map(DocumentSnapshot::getId)
                .collect(Collectors.toList());
    
            for (String playerId : playerIds) {
                if (!playerId.equals("emptyPlayerDoc")) {
                    System.out.println("Found Player: " + playerId);
                    players.add(playerId);
                }
            }
        } else {
            System.out.println("No player documents found");
        }
        return players;
    }
    


    public List<Tournament> getAllTournaments() throws ExecutionException, InterruptedException {
        // Retrieve all documents from the "tournament" collection
        ApiFuture<QuerySnapshot> future = firestore.collection("tournament").get();
        
        // QuerySnapshot contains all documents in the collection
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        
        // Create a list to hold the Tournament objects
        List<Tournament> tournaments = new ArrayList<>();
        
        // Convert each document to a Tournament object and add it to the list
        for (DocumentSnapshot document : documents) {
            Tournament tournament = document.toObject(Tournament.class);
            tournaments.add(tournament);
        }
        
        return tournaments;
    }

    public List<String> getAllTournamentNames() throws ExecutionException, InterruptedException {
        // Retrieve all documents from the "tournament" collection
        ApiFuture<QuerySnapshot> future = firestore.collection("tournament").get();
        
        // QuerySnapshot contains all documents in the collection
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        
        // Create a list to hold the Tournament objects
        List<String> tournaments = new ArrayList<>();
        
        // Convert each document to a Tournament object and add it to the list
        for (DocumentSnapshot document : documents) {
            String tournament = document.getId();

            tournaments.add(tournament);
        }
        
        return tournaments;
    }

    public List<String> getAllTournamentForPlayer(String playerName) throws ExecutionException, InterruptedException{
        List<String> result = new ArrayList<>();
        List<String> allTournamentNames = getAllTournamentNames();
        for (String tournament : allTournamentNames) {
            List<String> tournamentPlayers = getAllPlayer(tournament);
            for (String player : tournamentPlayers) {
                if (player.equals(playerName)) {
                    result.add(tournament);
                    continue;
                }
            }
        }
        return result;
    }    
    

    public String updateTournament(Tournament tournament) throws ExecutionException, InterruptedException { 
        if(tournamentValidator.isTournamentValid(tournament)){ // validations, will throw error if not valid
            System.out.println("Valid Input");
        }
        
        // Check if the document exists
        DocumentReference tournamentDocRef = firestore.collection("tournament").document(tournament.getTournamentName());
        ApiFuture<DocumentSnapshot> future = tournamentDocRef.get();
        DocumentSnapshot document = future.get();
        
        if (document.exists()) {
            // Document exists, proceed with the update
            ApiFuture<WriteResult> collectionsApiFuture = tournamentDocRef.set(tournament, SetOptions.merge()); // Update only new values
            return "Tournament updated at: " + collectionsApiFuture.get().getUpdateTime().toString();
        } else {
            return "Tournament not found with name: " + tournament.getTournamentName();
        }
    }
    

    // For this Firebase doc, the tournamentName is the documentId.
    public String deleteTournament(String tournamentName) throws ExecutionException, InterruptedException {
        // Reference the document in Firestore
        DocumentReference tournamentDocRef = firestore.collection("tournament").document(tournamentName);
        
        // Check if the document exists
        ApiFuture<DocumentSnapshot> future = tournamentDocRef.get();
        DocumentSnapshot document = future.get();
        
        if (!document.exists()) {
            return "Tournament not found: " + tournamentName;
        }
        
        // If the document exists, proceed with the deletion
        ApiFuture<WriteResult> writeResult = tournamentDocRef.delete();
        writeResult.get();  // Wait for the delete operation to complete
        
        return "Successfully deleted " + tournamentName;
    }

    // // CRUD for Rounds (nested under Tournament)

    public String createRound(String tournamentName, Round round) throws ExecutionException, InterruptedException{
        DocumentReference roundDocRef = firestore.collection("tournament")
                                                                 .document(tournamentName)
                                                                 .collection("round")
                                                                 .document(round.getRoundName());
        // add some field to Round x
        ApiFuture<WriteResult> roundResult = roundDocRef.set(new HashMap<>()); // Creating with an empty map
        roundResult.get(); // Wait for completion
        // iteratively create matches and to firestore. A round may or may not contain matches
        List<Match> matches = round.getMatches();
        int matchCounter = 0;
        for (Match match : matches) {
            String documentId = generateMatchId(tournamentName, round.getRoundName(), match);
            ApiFuture<WriteResult> matchUpdate = roundDocRef.collection("match").document(documentId).set(match);
            matchCounter++;
            matchUpdate.get();
        }
        
        return "Round " + round.getRoundName() + ", " + matchCounter +  " matches, created in " + tournamentName;
    }

    public Round getRound(String tournamentName, String roundName) throws ExecutionException, InterruptedException {
        System.out.println("getRound starting");
        System.out.println("arguments are " + tournamentName + ", " + roundName);
        
        DocumentReference documentReference = firestore.collection("tournament")
                                                         .document(tournamentName)
                                                         .collection("round")
                                                         .document(roundName);
    
        ApiFuture<DocumentSnapshot> future = documentReference.get();
        DocumentSnapshot document = future.get();
        Round round;
        
        if (document.exists()) {
            System.out.println("Round document exists");
            round = document.toObject(Round.class); // Convert to object
            round.setRoundName(roundName);
            
            // Fetching the "matches" subcollection
            CollectionReference matchSubcollectionRef = documentReference.collection("match");
            ApiFuture<QuerySnapshot> matchQuerySnapshotFuture = matchSubcollectionRef.get();
            QuerySnapshot matchQuerySnapshot = matchQuerySnapshotFuture.get();
            
            // Check if "matches" contains documents
            if (!matchQuerySnapshot.isEmpty()) {
                System.out.println("Found " + matchQuerySnapshot.size() + " match documents");
                
                // Map documents to Match objects
                List<Match> matches = matchQuerySnapshot.getDocuments().stream()
                                                        .map(doc -> doc.toObject(Match.class))
                                                        .collect(Collectors.toList());
                round.setMatches(matches);
            } else {
                System.out.println("No match documents found");
                round.setMatches(new ArrayList<>()); // Set empty list if no matches found
            }
            
            return round;
        }
        
        System.out.println("Round document does not exist");
        return null;
    }
    
    // round itself has no fields so no need update

    public String deleteRound(String tournamentName, String roundName) throws ExecutionException, InterruptedException {
        // Get a reference to the round document
        DocumentReference roundDocRef = firestore.collection("tournament")
                                                   .document(tournamentName)
                                                   .collection("round")
                                                   .document(roundName);
    
        System.out.println("Attempting to delete round: " + roundName + " from tournament: " + tournamentName);
    
        // Step 1: Delete all documents in the "matches" subcollection
        CollectionReference matchesSubcollection = roundDocRef.collection("match");
        ApiFuture<QuerySnapshot> matchesFuture = matchesSubcollection.get();
        List<QueryDocumentSnapshot> matches = matchesFuture.get().getDocuments();
        System.out.println("Found " + matches.size() + " match(es) to delete in 'matches' subcollection.");
        
        for (QueryDocumentSnapshot match : matches) {
            System.out.println("Deleting match: " + match.getId());
            match.getReference().delete().get();  // Adding .get() to ensure synchronous deletion
        }
    
        // Step 2: Delete all documents in the "standings" subcollection
        CollectionReference standingsSubcollection = roundDocRef.collection("standing");
        ApiFuture<QuerySnapshot> standingsFuture = standingsSubcollection.get();
        List<QueryDocumentSnapshot> standings = standingsFuture.get().getDocuments();
        System.out.println("Found " + standings.size() + " standing(s) to delete in 'standings' subcollection.");
        
        for (QueryDocumentSnapshot standing : standings) {
            System.out.println("Deleting standing: " + standing.getId());
            standing.getReference().delete().get();  // Adding .get() to ensure synchronous deletion
        }
    
        // Step 3: Delete the round document itself
        ApiFuture<WriteResult> deleteFuture = roundDocRef.delete();
        System.out.println("Deleted round document: " + roundName);
        
        return "Deleted round: " + roundName + ", time: " + deleteFuture.get().getUpdateTime().toString();
    }
    
    // // CRUD for Matches (nested under Round -> Tournament)
    // TODO some input validation for match? we have to make sure that match is created with two valid userNames
    // now for matches, winner wins losses isDraw isBye can be empty for now, but must be updated later. 

    public String roundEnd(String tournamentName, String roundName) throws ExecutionException, InterruptedException{
        //Round round = getRound(tournamentName, roundName);
        
        //processRoundData(tournamentName, round); //based on the matches in the round, go and update FB player's matches with the ID

        Tournament tournament = getTournament(tournamentName);
        if (tournament != null){


            tournament.setCurrentRound(Integer.parseInt(tournament.getCurrentRound())+1 + "");

            updateTournament(tournament);

            return "Round Number Updated";

        }else{
            return "Tournament not found";
        }
    }

    public String generateMatchId(String tournamentName, String roundName, Match match ) {
        String matchId = (tournamentName.trim() + "_" + roundName.trim() + "_" 
        + match.getPlayer1().trim() + "_" + match.getPlayer2().trim()).replaceAll("\\s+", "-");
        return matchId;
    }

    public String generateMatchId(String tournamentName, String roundName, String player1, String player2 ) {
        String matchId = (tournamentName.trim() + "_" + roundName.trim() + "_" 
        + player1.trim() + "_" + player2.trim()).replaceAll("\\s+", "-");
        return matchId;
    }

    public Map<String, String> splitMatchId(String matchId) {
        System.out.println(matchId);
        String[] parts = matchId.split("_");
    
        // Validate if the matchId is properly formatted
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid matchId format");
        }
    
        // Create a map to store the extracted values
        Map<String, String> result = new HashMap<>();
        result.put("tournamentName", parts[0]);
        result.put("roundName", parts[1]);
        result.put("player1", parts[2]);
        result.put("player2", parts[3]);
    
        return result;
    }

public void processRoundData(String tournamentName, Round round) throws InterruptedException, ExecutionException {
    String roundName = round.getRoundName(); // Get round name from the Round object
    List<Match> matches = round.getMatches(); // Get matches from the Round object

    for (Match match : matches) {
        String player1 = match.getPlayer1(); // Get player1
        String player2 = match.getPlayer2(); // Get player2

        if (player1 == null && player2 == null) {
            System.out.println("ignoring the empty match doc");
            continue;
        }

        // Generate matchId using your existing method
        String matchId = generateMatchId(tournamentName, roundName, match);

        // Update matches for each player if they are not null
        if (player1 != null) {
            updatePlayerMatch(tournamentName, player1, matchId);
        }
        if (player2 != null) {
            updatePlayerMatch(tournamentName, player2, matchId);
        }
    }
}
    
    

    public String createMatch(String tournamentName, String roundName, Match match) throws ExecutionException, InterruptedException{
        String documentId = generateMatchId(tournamentName, roundName, match);
        DocumentReference matchDocRef = firestore.collection("tournament")
                                                                 .document(tournamentName)
                                                                 .collection("round")
                                                                 .document(roundName)
                                                                 .collection("match")
                                                                 .document(documentId); // concated Id
                                                                 
        // put in the object into the created document
        ApiFuture<WriteResult> matchResult = matchDocRef.set(match); 
        matchResult.get(); // Wait for completion

        return "One match created in " + tournamentName + ", " + roundName + " at " + matchResult.get().getUpdateTime().toString();
    }

    public Match getMatch(String tournamentName, String roundName, String player1, String player2) throws ExecutionException, InterruptedException {
        System.out.println("getMatch starting");
        // Generate the same documentId used in createMatch
        String documentId = generateMatchId(tournamentName, roundName, player1, player2);
        System.out.println("DocumentId to be searched: " + documentId);
        DocumentReference matchDocRef = firestore.collection("tournament")
                                                   .document(tournamentName)
                                                   .collection("round")
                                                   .document(roundName)
                                                   .collection("match")
                                                   .document(documentId);
    
        // Fetch the match document
        ApiFuture<DocumentSnapshot> future = matchDocRef.get();
        DocumentSnapshot document = future.get();
    
        if (document.exists()) {
            System.out.println("Document exists");
            // Convert the document back to a Match object
            return document.toObject(Match.class);
        } else {
            System.out.println("Match not found for: " + documentId);
            return null; // Or throw an exception depending on how you want to handle it
        }
    }

    public String updateMatch(String tournamentName, String roundName, String player1, String player2, Match updatedMatch) throws ExecutionException, InterruptedException {
        // Generate the documentId based on tournament, round, and player names

        String documentId = generateMatchId(tournamentName, roundName, player1, player2);
        
        DocumentReference matchDocRef = firestore.collection("tournament")
                                                   .document(tournamentName)
                                                   .collection("round")
                                                   .document(roundName)
                                                   .collection("match")
                                                   .document(documentId);
        
        // Check if the match document exists
        ApiFuture<DocumentSnapshot> future = matchDocRef.get();
        DocumentSnapshot document = future.get();
        
        if (document.exists()) {
            // Document exists, proceed with the update
            ApiFuture<WriteResult> matchResult = matchDocRef.set(updatedMatch, SetOptions.merge()); // Update only provided fields
            matchResult.get(); // Wait for completion
            return "Match updated for " + player1 + " vs " + player2 + " in " + tournamentName + " at " + roundName;
        } else {
            // Document does not exist, return a message
            return "Match not found for " + player1 + " vs " + player2 + " in " + tournamentName + " at " + roundName;
        }
    }
    

    public String deleteMatch(String tournamentName, String roundName, String player1, String player2) throws ExecutionException, InterruptedException {
        // Generate the documentId based on tournament, round, and player names
        String documentId = generateMatchId(tournamentName, roundName, player1, player2);
    
        DocumentReference matchDocRef = firestore.collection("tournament")
                                                   .document(tournamentName)
                                                   .collection("round")
                                                   .document(roundName)
                                                   .collection("match")
                                                   .document(documentId);
    
        // Delete the match document
        ApiFuture<WriteResult> deleteResult = matchDocRef.delete();
        deleteResult.get(); // Wait for completion
    
        return "Match deleted for " + player1 + " vs " + player2 + " in " + tournamentName + " at " + roundName;
    }
    



    // // CRUD for Standings (nested under Round -> Tournament)

    public String createStanding(String tournamentName, String roundName, Standing standing) throws ExecutionException, InterruptedException {
        //String documentId = String.valueOf(standing.getRank()); // Using rank as the document ID
        DocumentReference standingDocRef = firestore.collection("tournament")
                                                      .document(tournamentName)
                                                      .collection("round")
                                                      .document(roundName)
                                                      .collection("standing")
                                                      .document();
    
        ApiFuture<WriteResult> standingResult = standingDocRef.set(standing);
        standingResult.get(); // Wait for completion
    
        return "One standing created in " + tournamentName + ", " + roundName + " at " + standingResult.get().getUpdateTime().toString();
    }

    public Standing getStanding(String tournamentName, String roundName, int rank) throws ExecutionException, InterruptedException {
        String documentId = String.valueOf(rank); // Use rank as the document ID
        DocumentReference standingDocRef = firestore.collection("tournament")
                                                      .document(tournamentName)
                                                      .collection("round")
                                                      .document(roundName)
                                                      .collection("standing")
                                                      .document(documentId);
    
        ApiFuture<DocumentSnapshot> future = standingDocRef.get();
        DocumentSnapshot document = future.get();
    
        if (document.exists()) {
            return document.toObject(Standing.class);
        } else {
            return null; // Or handle error
        }
    }

    public List<Standing> getAllStanding(String tournamentName, String roundName) throws ExecutionException, InterruptedException {
        // Retrieve all documents from the "tournament" collection
        ApiFuture<QuerySnapshot> future = firestore.collection("tournament")
                                                    .document(tournamentName)
                                                    .collection("round")
                                                    .document(roundName)
                                                    .collection("standing")
                                                    .get();
        
        // QuerySnapshot contains all documents in the collection
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        
        // Create a list to hold the Tournament objects
        List<Standing> standings = new ArrayList<>();
        
        // Convert each document to a Tournament object and add it to the list
        for (DocumentSnapshot document : documents) {
            Standing standing = document.toObject(Standing.class);
            standings.add(standing);
        }
        
        return standings;
    }
        
    public String updateStanding(String tournamentName, String roundName, Standing updatedStanding) throws ExecutionException, InterruptedException {
        // Generate the documentId based on tournament, round, and rank
        String documentId = updatedStanding.getRank() + "";
        
        DocumentReference standingDocRef = firestore.collection("tournament")
                                                      .document(tournamentName)
                                                      .collection("round")
                                                      .document(roundName)
                                                      .collection("standing")
                                                      .document(documentId);
    
        // Update the standing document only for the fields that are provided (merge operation)
        ApiFuture<WriteResult> standingResult = standingDocRef.set(updatedStanding, SetOptions.merge()); // Only update the provided fields
        standingResult.get(); // Wait for completion
    
        return "Standing updated for rank " + documentId + " in " + tournamentName + " at " + roundName;
    }
    
    
    public String deleteStanding(String tournamentName, String roundName, int rank) throws ExecutionException, InterruptedException {
        String documentId = String.valueOf(rank); // Use rank as the document ID

        DocumentReference standingDocRef = firestore.collection("tournament")
                                                      .document(tournamentName)
                                                      .collection("round")
                                                      .document(roundName)
                                                      .collection("standing")
                                                      .document(documentId);
    
        ApiFuture<WriteResult> deleteResult = standingDocRef.delete();
        deleteResult.get(); // Wait for completion
    
        return "Standing with rank " + rank + " deleted from " + tournamentName + ", " + roundName;
    }

    public boolean startTournament( String tournament) throws ExecutionException, InterruptedException{
        
        Tournament tourney = getTournament(tournament);

        if (tourney != null){
            tourney.setInProgress(true);
            updateTournament(tourney);
            List<String> players= tourney.getParticipatingPlayers();

            ArrayList<AlgoTournamentPlayer> playerObjs = new ArrayList<AlgoTournamentPlayer>();

            for (String s : players){
                playerObjs.add(new AlgoTournamentPlayer(s, new ArrayList<AlgoMatch>()));
            }

            AlgoRound rd1 = new AlgoRound(1, playerObjs);

            rd1.generateRoundOne();

            tourney.setCurrentRound("1");

            Round emptyround = new Round();
            emptyround.setMatches(new ArrayList<>());
            emptyround.setRoundName("1");
            createRound(tournament, emptyround);
            updateTournament(tourney);

            ArrayList<AlgoMatch> Rd1Matches = rd1.getAlgoMatches();


            for (AlgoMatch algoMatch : Rd1Matches){
                
                // create new match 

                Match match = new Match();
                String player1Name = algoMatch.getPlayer1().getPlayerID();

                // if is bye

                if (algoMatch.isBye()){
                    match.setPlayer1(player1Name);
                    match.setBye(true);

                    //updates player match record
                    updatePlayerMatch(tournament,player1Name ,generateMatchId(tournament, "1",match));
                    
                }else{

                    String player2Name = algoMatch.getPlayer2().getPlayerID();
                    match.setPlayer1(player1Name);
                    match.setPlayer2(player2Name);
                    match.setBye(false);

                    //updates player match record
                    updatePlayerMatch(tournament,player1Name ,generateMatchId(tournament, "1",match));
                    updatePlayerMatch(tournament, player2Name, generateMatchId(tournament,"1", match));

                }

                createMatch(tournament, "1", match);

            }

            return true;

        }   

        return false;

    }
    


    public boolean generateRound(String tournament)throws ExecutionException, InterruptedException{

        Tournament tourney = getTournament(tournament);

        if (tourney != null){


            // input all participating players into algoObjs and put them into the list algoPlayers

            List<String> players= tourney.getParticipatingPlayers();
            ArrayList<AlgoTournamentPlayer> algoPlayers = new ArrayList<AlgoTournamentPlayer>();

            HashMap<AlgoTournamentPlayer , List<String>> playerToPastMatches = new HashMap<AlgoTournamentPlayer , List<String>>();
            HashMap<String , AlgoTournamentPlayer > playerIDToObj = new HashMap<String , AlgoTournamentPlayer>();
            
            for (String playerName : players){
                ParticipatingPlayer playerData = getPlayer(tournament, playerName);
                AlgoTournamentPlayer algoPlayer = new AlgoTournamentPlayer( playerName, new ArrayList<AlgoMatch>());
                algoPlayers.add(algoPlayer);

                System.out.println("playername : " + playerData.getUserName());
                System.out.println("playerobj : " + algoPlayer.getPlayerID());
                playerToPastMatches.put( algoPlayer, playerData.getPastMatches());
                playerIDToObj.put(playerName, algoPlayer);

            }

            // update all algoMatchObjs with appropriate player objs

            for (AlgoTournamentPlayer player : algoPlayers){

                int rdno = 1;

                for ( String matchID : playerToPastMatches.get(player)){

                    Match matchData = getMatch(tournament,""+rdno++ , matchID);
                    AlgoMatch algoMatchtoAdd;

                    if (matchData.isBye()){
                        algoMatchtoAdd = new AlgoMatch( playerIDToObj.get(matchData.getPlayer1()));

                    }else{

                        System.out.println(matchData.getPlayer1());
                        System.out.println(matchData.getPlayer2());

                        AlgoTournamentPlayer p1 = playerIDToObj.get(matchData.getPlayer1());
                        AlgoTournamentPlayer p2 = playerIDToObj.get(matchData.getPlayer2());

                        System.out.println("player1 is "+p1);
                        System.out.println(p2);
                            
                        algoMatchtoAdd = new AlgoMatch(p1,p2);
                        int wins = matchData.getWins();
                        int losses = matchData.getLosses();
                        algoMatchtoAdd.update(playerIDToObj.get(matchData.getWinner()), wins, losses );

                    }

                    player.addMatch(algoMatchtoAdd);

                }

                //System.out.println("players inside :" + player.getPlayerID());
            }

            // generate standings and update DB

            AlgoRound algoRound = new AlgoRound(Integer.parseInt(tourney.getCurrentRound()), algoPlayers);
            algoRound.generateStandings();

            AlgoStandings prevRoundStandings = algoRound.getStandings();


            int rank = 1;

            for (AlgoTournamentPlayer player : prevRoundStandings.getStandings()){
                
                Standing playerCurStanding = new Standing();
                
                playerCurStanding.setRank(rank++);
                playerCurStanding.setCurGamePts(player.getCurMatchPts());
                playerCurStanding.setCurMatchPts(player.getCurMatchPts());
                playerCurStanding.setCurOGW(player.getCurOGW());
                playerCurStanding.setCurOMW(player.getCurOMW());
                playerCurStanding.setPlayerID(player.getPlayerID());

                createStanding(tournament, Integer.parseInt(tourney.getCurrentRound())-1 + "", playerCurStanding);

            }


            // generate rounds and update DB

            List<Match> roundMatches = new ArrayList<Match>();

            algoRound.generateAlgoMatches();

            for ( AlgoMatch matchObj : algoRound.getAlgoMatches()){

                Match newMatch = new Match();

                String p1name = matchObj.getPlayer1().getPlayerID();

                newMatch.setPlayer1(p1name);

                if (matchObj.isBye()){
                    newMatch.setBye(true);
                    newMatch.setWins(2);
                    newMatch.setWinner(p1name);
                }else{
                    newMatch.setPlayer2(matchObj.getPlayer2().getPlayerID());
                }

                roundMatches.add(newMatch);

            }

            // DB specific functionality to add the round

            Round newRound = new Round();
            newRound.setMatches(roundMatches);
            newRound.setRoundName(Integer.parseInt(tourney.getCurrentRound())+ "");
            createRound(tournament,newRound);


            return true;


        }
        return false;
    }



    // specific getter for specific match

    public Match getMatch(String tournamentName, String roundName, String documentId) throws ExecutionException, InterruptedException {
        System.out.println("getMatch starting");
        // Generate the same documentId used in createMatch
        System.out.println("DocumentId to be searched: " + documentId);
        DocumentReference matchDocRef = firestore.collection("tournament")
                                                   .document(tournamentName)
                                                   .collection("round")
                                                   .document(roundName)
                                                   .collection("match")
                                                   .document(documentId);
    
        // Fetch the match document
        ApiFuture<DocumentSnapshot> future = matchDocRef.get();
        DocumentSnapshot document = future.get();
    
        if (document.exists()) {
            System.out.println("Document exists");
            // Convert the document back to a Match object
            return document.toObject(Match.class);
        } else {
            System.out.println("Match not found for: " + documentId);
            return null; // Or throw an exception depending on how you want to handle it
        }
    }

    public Match getMatchByPlayer(String tournamentName, String roundName, String player) throws ExecutionException, InterruptedException {

        //Search both player 1 and player 2 to find match

        ApiFuture<QuerySnapshot> queryp1 = firestore.collection("tournament")
                                                  .document(tournamentName)
                                                  .collection("round")
                                                  .document(roundName)
                                                  .collection("match")
                                                  .whereEqualTo("player1", player)
                                                  .get();  // Search field by player

        ApiFuture<QuerySnapshot> queryp2 = firestore.collection("tournament")
                                                  .document(tournamentName)
                                                  .collection("round")
                                                  .document(roundName)
                                                  .collection("match")
                                                  .whereEqualTo("player2", player)
                                                  .get();  // Search field by player
            
        
        // Get the matching documents

        List<QueryDocumentSnapshot> p1search = queryp1.get().getDocuments();
        List<QueryDocumentSnapshot> p2search = queryp2.get().getDocuments();

        if (p1search.isEmpty() && p2search.isEmpty()){
            return null;
        }
        QueryDocumentSnapshot matchdoc = p1search.isEmpty() ? p2search.get(0) : p1search.get(0);

        return matchdoc.toObject(Match.class);
        
    }





    
}
