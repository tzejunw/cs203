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



import java.util.stream.Collectors;

@Service
public class TournamentService {

    // CRUD for Tournaments
    // This takes one of the specified json fields, here .getTournamentName(),  and sets it as the documentId (document identifier)
    // if you want firebase to generate documentId for us, leave .document() blank
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
        // iteratively create matches and to firestore
        List<Match> matches = round.getMatches();
        int matchCounter = 0;
        for (Match match : matches) {
            ApiFuture<WriteResult> matchUpdate =  roundDocRef.collection("match").document().set(match);
            matchCounter++;
            matchUpdate.get();
        }
        
        // Add an empty document to "standings"
        ApiFuture<WriteResult> standingsUpdate =  roundDocRef.collection("standing").document("emptyStandingsDoc").set(new HashMap<>());        
        
        return round.getRoundName() + ", "+ matchCounter +  " matches, empty standings collection, created at " + standingsUpdate.get().getUpdateTime().toString();
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
    // now for matches, i want to be able to update winner wins losses isDraw isBye


    // // CRUD for Standings (nested under Round -> Tournament)

}
