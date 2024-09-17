package com.java.firebase.demo.tournament;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.*;
import java.util.concurrent.ExecutionException;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;

import java.util.stream.Collectors;

@Service
public class TournamentService {

    // CRUD for Tournaments
    // This takes one of the specified json fields, here .getTournamentName(),  and sets it as the documentId (document identifier)
    // if you want firebase to generate documentId for us, leave .document() blank
    // generates a tournament with an empty Round 1
    public String createTournament(Tournament tournament) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        
        // Add the tournament object as a document in Firestore
        ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection("tournament")
                                                                  .document(tournament.getTournamentName())
                                                                  .set(tournament);
        
        // Wait for the tournament document to be created
        collectionsApiFuture.get();
        
        // Create the "rounds" subcollection under the tournament document, with the first round
        DocumentReference round1DocRef = dbFirestore.collection("tournament")
                                                    .document(tournament.getTournamentName())
                                                    .collection("round")
                                                    .document("Round 1");
        
        ApiFuture<WriteResult> round1Result = round1DocRef.set(new HashMap<>()); // Creating with an empty map
        round1Result.get(); // Wait for completion
        // Create empty subcollections "matches" and "standings" under the "round1" document
        // Note: Firestore does not store empty collections, so you need to create at least an empty document or field
    
        // Add an empty document to "match"
        round1DocRef.collection("match").document("emptyMatchDoc").set(new HashMap<>());
        
        // Add an empty document to "standings"
        round1DocRef.collection("standing").document("emptyStandingsDoc").set(new HashMap<>());
        
        return collectionsApiFuture.get().getUpdateTime().toString();
    }
    
    // For this Firebase doc, the tournamentName is the documentId.
    // The key in the GET request must be "documentid", and the value is the tournamentName
    public Tournament getTournament(String documentId) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference documentReference = dbFirestore.collection("tournament").document(documentId); 
        ApiFuture<DocumentSnapshot> future = documentReference.get();
        DocumentSnapshot document = future.get();
        Tournament tournament;
        if (document.exists()) {
            tournament = document.toObject(Tournament.class); // convert to object
            return tournament;
        }
        return null;
    }

    // this is exact same as POST route!! should we input validate?
    // TODO input validate that the json has "documentId" and that its value pair exisits in the database!
    public String updateTournament(Tournament tournament) throws ExecutionException, InterruptedException{ 
        Firestore dbFirestore = FirestoreClient.getFirestore(); // connect the db
        ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection("tournament").document(tournament.getTournamentName()).set(tournament); // takes name to be primary key
        return collectionsApiFuture.get().getUpdateTime().toString();
    }

    // For this Firebase doc, the tournamentName is the documentId.
    // The key in the GET request must be "documentid", and the value is the tournamentName
    public String deleteTournament(String documentId) throws ExecutionException, InterruptedException {
        // we are using email as the documentId. the key in the DELETE request must be "documentid", and the value is the email
        Firestore dbFirestore = FirestoreClient.getFirestore(); // connect the db
        ApiFuture<WriteResult> writeResult = dbFirestore.collection("tournament").document(documentId).delete(); // get the doc
        return "Successfully deleted " + documentId;
    }

    // // CRUD for Rounds (nested under Tournament)

    public String createRound(String tournamentName, Round round) throws ExecutionException, InterruptedException{
        Firestore dbFirestore = FirestoreClient.getFirestore(); 
        DocumentReference roundDocRef = dbFirestore.collection("tournament")
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
            + match.getPlayer1().trim() + "_" + match.getPlayer2().trim()).replaceAll("\\s+", "_");
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
        
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference documentReference = dbFirestore.collection("tournament")
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
        Firestore dbFirestore = FirestoreClient.getFirestore();
        
        // Get a reference to the round document
        DocumentReference roundDocRef = dbFirestore.collection("tournament")
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
    // TODO some input validation for match?
    // now for matches, i want to be able to update winner wins losses isDraw isBye
    public String createMatch(String tournamentName, String roundName, Match match) throws ExecutionException, InterruptedException{
        String documentId = (tournamentName.trim() + "_" + roundName.trim() + "_" 
        + match.getPlayer1().trim() + "_" + match.getPlayer2().trim()).replaceAll("\\s+", "_");
        Firestore dbFirestore = FirestoreClient.getFirestore(); 
        DocumentReference matchDocRef = dbFirestore.collection("tournament")
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
                             + player1.trim() + "_" + player2.trim()).replaceAll("\\s+", "_");
        System.out.println("DocumentId to be searched: " + documentId);
        Firestore dbFirestore = FirestoreClient.getFirestore(); 
        DocumentReference matchDocRef = dbFirestore.collection("tournament")
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
                             + player1.trim() + "_" + player2.trim()).replaceAll("\\s+", "_");
        
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference matchDocRef = dbFirestore.collection("tournament")
                                                   .document(tournamentName)
                                                   .collection("round")
                                                   .document(roundName)
                                                   .collection("match")
                                                   .document(documentId);
    
        // Update the match document
        ApiFuture<WriteResult> matchResult = matchDocRef.set(updatedMatch, SetOptions.merge()); // Only update the provided fields
        matchResult.get(); // Wait for completion
    
        return "Match updated for " + player1 + " vs " + player2 + " in " + tournamentName + " at " + roundName;
    }

    public String deleteMatch(String tournamentName, String roundName, String player1, String player2) throws ExecutionException, InterruptedException {
        // Generate the documentId based on tournament, round, and player names
        String documentId = (tournamentName.trim() + "_" + roundName.trim() + "_" 
                             + player1.trim() + "_" + player2.trim()).replaceAll("\\s+", "_");
    
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference matchDocRef = dbFirestore.collection("tournament")
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
        String documentId = String.valueOf(standing.getRank()); // Using rank as the document ID
        Firestore dbFirestore = FirestoreClient.getFirestore(); 
        DocumentReference standingDocRef = dbFirestore.collection("tournament")
                                                      .document(tournamentName)
                                                      .collection("round")
                                                      .document(roundName)
                                                      .collection("standing")
                                                      .document(documentId);
    
        ApiFuture<WriteResult> standingResult = standingDocRef.set(standing);
        standingResult.get(); // Wait for completion
    
        return "One standing created in " + tournamentName + ", " + roundName + " at " + standingResult.get().getUpdateTime().toString();
    }

    public Standing getStanding(String tournamentName, String roundName, int rank) throws ExecutionException, InterruptedException {
        String documentId = String.valueOf(rank); // Use rank as the document ID
        Firestore dbFirestore = FirestoreClient.getFirestore(); 
        DocumentReference standingDocRef = dbFirestore.collection("tournament")
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
        
    public String updateStanding(String tournamentName, String roundName, Standing updatedStanding) throws ExecutionException, InterruptedException {
        // Generate the documentId based on tournament, round, and rank
        String documentId = updatedStanding.getRank() + "";
        
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference standingDocRef = dbFirestore.collection("tournament")
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
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference standingDocRef = dbFirestore.collection("tournament")
                                                      .document(tournamentName)
                                                      .collection("round")
                                                      .document(roundName)
                                                      .collection("standing")
                                                      .document(documentId);
    
        ApiFuture<WriteResult> deleteResult = standingDocRef.delete();
        deleteResult.get(); // Wait for completion
    
        return "Standing with rank " + rank + " deleted from " + tournamentName + ", " + roundName;
    }
    
}
