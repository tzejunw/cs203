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
import com.java.firebase.demo.algo.AlgoTournamentPlayer;

import io.github.cdimascio.dotenv.Dotenv;

@Service
public class TournamentService {
    private final Firestore firestore;

    public TournamentService(Firestore firestore) {
        this.firestore = firestore;
    }

    public static boolean isValidDate(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            // Parse the date string using the formatter
            LocalDate date = LocalDate.parse(dateStr, formatter);
            // Ensure the parsed date string matches the input (to avoid things like 2024-2-30)
            return dateStr.equals(date.format(formatter));
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static boolean areDatesInOrder(String startDateStr, String endDateStr) {
        // Parse the dates after confirming they are in the correct format
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);

        // Check that the end date is not earlier than the start date
        return !endDate.isBefore(startDate);
    }

    public boolean isNameUnique(String name) throws InterruptedException, ExecutionException, FirestoreException {
        try {
            // Get the document from Firestore
            ApiFuture<DocumentSnapshot> future = firestore.collection("tournament").document("name").get();
            DocumentSnapshot document = future.get();

            // Return true if the document does NOT exist (ID is unique), otherwise false
            return !document.exists();
        } catch (InterruptedException | ExecutionException | FirestoreException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isValidAddress(String address) {
        Dotenv dotenv = Dotenv.load();
        String map_api_key = dotenv.get("GOOGLE_MAP_API_KEY");
        String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + address + "&key=" + map_api_key;

        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                
                if (responseBody != null) {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode json = mapper.readTree(responseBody);
                    String status = json.get("status").asText();

                    // Check if the status is OK, indicating the address is valid
                    System.out.println("OK".equals(status));
                    return "OK".equals(status);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean isTournamentValid(Tournament tournament) throws IllegalArgumentException, InterruptedException, ExecutionException, FirestoreException {
        if (!isValidDate(tournament.getStartDate()))
            throw new IllegalArgumentException("Start date should be in yyyy-MM-dd format");
        if (!isValidDate(tournament.getEndDate()))
            throw new IllegalArgumentException("End date should be in yyyy-MM-dd format");
        if (!isValidDate(tournament.getRegistrationDeadline()))
            throw new IllegalArgumentException("End date should be in yyyy-MM-dd format");
        if (!areDatesInOrder(tournament.getStartDate(), tournament.getEndDate())){
            throw new IllegalArgumentException("End date should not be earlier than the start date");
        }
        if (!areDatesInOrder(tournament.getRegistrationDeadline(), tournament.getStartDate())){
            throw new IllegalArgumentException("Start date should not be earlier than the registration dateline");
        }
        if (Strings.isNullOrEmpty(tournament.getTournamentDesc())){
            throw new IllegalArgumentException("Tournament desc should not be empty");
        }
        if (Strings.isNullOrEmpty(tournament.getImageUrl())){
            throw new IllegalArgumentException("Tournament desc should not be empty");
        }
        if (Strings.isNullOrEmpty(tournament.getLocation()) || !isValidAddress(tournament.getLocation())){
            throw new IllegalArgumentException("Invalid location.");
        }
        if (Strings.isNullOrEmpty(tournament.getTournamentName())){
            throw new IllegalArgumentException("Invalid tournament name.");
        }
        return true;
    }

    // CRUD for Tournaments
    // This takes one of the specified json fields, here .getTournamentName(),  and sets it as the documentId (document identifier)
    // if you want firebase to generate documentId for us, leave .document() blank
    // generates a tournament with an empty Round 1
    public String createTournament(Tournament tournament) throws ExecutionException, InterruptedException {
        if(isTournamentValid(tournament)) {
            System.out.println("Valid Input"); // validations, will throw error if not valid
        }
        if (!isNameUnique(tournament.getTournamentName())){
            throw new IllegalArgumentException("The same tournament name already exists.");
        }
        
        // Add the tournament object as a document in Firestore
        ApiFuture<WriteResult> collectionsApiFuture = firestore.collection("tournament")
                                                                  .document(tournament.getTournamentName())
                                                                  .set(tournament);
        
        // Wait for the tournament document to be created
        collectionsApiFuture.get();

        // call the image functions here?
        // yes sire
        
        // Create the "rounds" subcollection under the tournament document, with the first round
        DocumentReference round1DocRef = firestore.collection("tournament")
                                                    .document(tournament.getTournamentName())
                                                    .collection("round")
                                                    .document("1");
        
        ApiFuture<WriteResult> round1Result = round1DocRef.set(new HashMap<>()); // Creating with an empty map
        round1Result.get(); // Wait for completion
        // Create empty subcollections "matches" and "standings" under the "round1" document
        // Note: Firestore does not store empty collections, so you need to create at least an empty document or field
    
        // Add an empty document to "match"
        round1DocRef.collection("match").document("emptyMatchDoc").set(new HashMap<>());
        
        // Add an empty document to "standings"
        round1DocRef.collection("standing").document("emptyStandingsDoc").set(new HashMap<>());

        // Create the "rounds" subcollection under the tournament document, with the first round
        DocumentReference participatingPlayerDocRef = firestore.collection("tournament")
                                                    .document(tournament.getTournamentName())
                                                    .collection("participatingPlayers")
                                                    .document("emptyPlayerDoc");
        ApiFuture<WriteResult> participatingPlayersResult = participatingPlayerDocRef.set(new HashMap<>()); // Creating with an empty map
        participatingPlayersResult.get(); // Wait for completion
        
        return collectionsApiFuture.get().getUpdateTime().toString();
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
        System.out.println("Player documetn found");
        participatingPlayer = document.toObject(ParticipatingPlayer.class); // Can be converted to JSON if required
    } else {
        System.out.println("player not found");
        participatingPlayer = new ParticipatingPlayer();
        participatingPlayer.setPastMatches(new ArrayList<>());
    }
    return participatingPlayer;
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
    
    // For this Firebase doc, the tournamentName is the documentId.
    // The key in the GET request must be "documentid", and the value is the tournamentName
    public Tournament getTournament(String tournamentName) throws ExecutionException, InterruptedException {
        DocumentReference documentReference = firestore.collection("tournament").document(tournamentName); 
        ApiFuture<DocumentSnapshot> future = documentReference.get();
        DocumentSnapshot document = future.get();
        Tournament tournament;
        if (document.exists()) {
            tournament = document.toObject(Tournament.class); // convert to object
            // go get the round objects in its subcollection.
            // Fetch the "round" subcollection
            CollectionReference roundsCollection = documentReference.collection("round");
            ApiFuture<QuerySnapshot> roundsFuture = roundsCollection.get();
            QuerySnapshot roundsSnapshot = roundsFuture.get();

            if (!roundsSnapshot.isEmpty()) {
                System.out.println("Found " + roundsSnapshot.size() + " round documents");

                // Get the IDs of the round documents
                List<String> roundIds = roundsSnapshot.getDocuments().stream()
                    .map(doc -> doc.getId()) // Get the document ID
                    .collect(Collectors.toList());

                // call getRound() on each roundId
                List<Round> rounds = new ArrayList<>();
                for (String roundName : roundIds) {
                    Round round = getRound(tournamentName, roundName);
                    rounds.add(round);
                }

                // Assuming the Tournament class has a method to set the list of round IDs
                tournament.setRounds(rounds); // or tournament.roundIds = roundIds;
            } else {
                System.out.println("No round documents found");
                tournament.setRounds(new ArrayList<>()); // Set empty list if no rounds found
            }

            return tournament;
        }
        return null;
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
    

    public String updateTournament(Tournament tournament) throws ExecutionException, InterruptedException { 
        if(isTournamentValid(tournament)) // validations, will throw error if not valid
        {
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
        // we are using email as the documentId. the key in the DELETE request must be "documentid", and the value is the email
        ApiFuture<WriteResult> writeResult = firestore.collection("tournament").document(tournamentName).delete(); // get the doc
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
        ApiFuture<WriteResult> matchUpdate =  roundDocRef.collection("match").document("emptyMatchDoc").set(new HashMap<>());
        List<Match> matches = round.getMatches();
        int matchCounter = 0;
        for (Match match : matches) {
            String documentId = (tournamentName.trim() + "_" + round.getRoundName().trim() + "_" 
            + match.getPlayer1().trim() + "_" + match.getPlayer2().trim()).replaceAll("\\s+", "-");
            matchUpdate = roundDocRef.collection("match").document(documentId).set(match);
            matchCounter++;
            matchUpdate.get();
        }
        
        // Add an empty document to "standings"
        ApiFuture<WriteResult> standingsUpdate =  roundDocRef.collection("standing").document("emptyStandingsDoc").set(new HashMap<>());
        
        return round.getRoundName() + "," + matchCounter +  " matches, empty standings collection, created in " + tournamentName + "at " + standingsUpdate.get().getUpdateTime().toString();
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
                System.out.println("Found " + matchQuerySnapshot.size() + " match documents, one of which is a empty placeholder");
                
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
        Round round = getRound(tournamentName, roundName);
        
        processRoundData(tournamentName, round); //based on the matches in the round, go and update FB player's matches with the ID


        return "Matches Assigned to participatingPlayer's pastMatches";
    }

    public String generateMatchId(String tournamentName, String roundName, Match match ) {
        String matchId = (tournamentName.trim() + "_" + roundName.trim() + "_" 
        + match.getPlayer1().trim() + "_" + match.getPlayer2().trim()).replaceAll("\\s+", "-");
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
        String documentId = (tournamentName.trim() + "_" + roundName.trim() + "_" 
                             + player1.trim() + "_" + player2.trim()).replaceAll("\\s+", "-");
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
        String documentId = (tournamentName.trim() + "_" + roundName.trim() + "_" 
                             + player1.trim() + "_" + player2.trim()).replaceAll("\\s+", "-");
        
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
        String documentId = (tournamentName.trim() + "_" + roundName.trim() + "_" 
                             + player1.trim() + "_" + player2.trim()).replaceAll("\\s+", "-");
    
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
            tourney.setInProgress("true");
            updateTournament(tourney);
            List<String> players= tourney.getParticipatingPlayers();

            ArrayList<AlgoTournamentPlayer> playerObjs = new ArrayList<AlgoTournamentPlayer>();

            for (String s : players){
                playerObjs.add(new AlgoTournamentPlayer(s, new ArrayList<AlgoMatch>()));
            }

            AlgoRound rd1 = new AlgoRound(1, playerObjs);

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
                    updatePlayerMatch(tournament,player1Name ,generateMatchId(tournament, "round1",match));
                    
                }else{

                    String player2Name = algoMatch.getPlayer2().getPlayerID();
                    match.setPlayer1(player1Name);
                    match.setPlayer2(player2Name);
                    match.setBye(false);

                    //updates player match record
                    updatePlayerMatch(tournament,player1Name ,generateMatchId(tournament, "round1",match));
                    updatePlayerMatch(tournament, player2Name, generateMatchId(tournament,"round1", match));

                }

                createMatch(tournament, "round1", match);

            }

            return true;
        }   

        return false;

    }

    public boolean generateRound(String tournament)throws ExecutionException, InterruptedException{

        Tournament tourney = getTournament(tournament);

        if (tourney != null){

            List<String> players= tourney.getParticipatingPlayers();
            ArrayList<AlgoTournamentPlayer> algoPlayers = new ArrayList<AlgoTournamentPlayer>();

            HashMap<AlgoTournamentPlayer , List<String>> playerToPastMatches = new HashMap<AlgoTournamentPlayer , List<String>>();
            HashMap<String , AlgoTournamentPlayer > playerIDToObj = new HashMap<String , AlgoTournamentPlayer>();
            
            for (String playerName : players){
                ParticipatingPlayer playerData = getPlayer(tournament, playerName);
                AlgoTournamentPlayer algoPlayer = new AlgoTournamentPlayer( playerData.getUserName(), new ArrayList<AlgoMatch>());
                algoPlayers.add(algoPlayer);
                playerToPastMatches.put( algoPlayer, playerData.getPastMatches());
                playerIDToObj.put(playerData.getUserName(), algoPlayer);
            }

            for (AlgoTournamentPlayer player : algoPlayers){

                for ( String matchID : playerToPastMatches.get(player)){

                    Match matchData = getMatch(matchID);
                    AlgoMatch algoMatchtoAdd;

                    if (matchData.isBye()){
                        algoMatchtoAdd = new AlgoMatch( playerIDToObj.get(matchData.getPlayer1()));
                        


                    }else{
                        AlgoTournamentPlayer p1 = playerIDToObj.get(matchData.getPlayer1());
                        AlgoTournamentPlayer p2 = playerIDToObj.get(matchData.getPlayer2());
                            
                        algoMatchtoAdd = new AlgoMatch(p1,p2);
                        int wins = matchData.getWins();
                        int losses = matchData.getLosses();
                        algoMatchtoAdd.update(playerIDToObj.get(matchData.getWinner()), wins, losses );

                    }

                    player.addMatch(algoMatchtoAdd);

                }
            }

            

            AlgoRound algoRound = new AlgoRound(Integer.parseInt(tourney.getCurrentRound()), algoPlayers);
            algoRound.generateStandings();

            int rank = 1;

            for (AlgoTournamentPlayer player : algoPlayers){
                
                Standing playerCurStanding = new Standing();
                
                playerCurStanding.setRank(rank++);
                playerCurStanding.setCurGamePts(player.getCurMatchPts());
                playerCurStanding.setCurMatchPts(player.getCurMatchPts());
                playerCurStanding.setCurOGW(player.getCurOGW());
                playerCurStanding.setCurOMW(player.getCurOMW());

                createStanding(tournament, tourney.getCurrentRound(), playerCurStanding);

            }

            ArrayList<AlgoMatch> roundMatches = new ArrayList<AlgoMatch>();

            algoRound.generateAlgoMatches();

            for ( AlgoMatch matchObj : algoRound.getAlgoMatches()){

                Match newMatch = new Match();

                newMatch.setPlayer1(matchObj.getPlayer1().getPlayerID());

                if (matchObj.isBye()){
                    newMatch.setBye(true);
                }else{
                    newMatch.setPlayer2(matchObj.getPlayer2().getPlayerID());
                }

                createMatch(tournament, tourney.getCurrentRound(), newMatch);

            }



        }
        return false;
    }



    
}
