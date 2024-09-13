package com.java.firebase.demo.tournament;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.ExecutionException;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.WriteResult;

@Service
public class TournamentService {

    // CRUD for Tournaments
    public String createTournament(Tournament tournament) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore(); 
        // documentId (identifier)is based on what we gave, .getEmail(). 
        //if you want firebase to generate for us, leave .document() blank
        // if you submit another create req with the same "documentId" now, it will overwrite
        ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection("tournament").document(tournament.getTournamentName()).set(tournament);
        
        return collectionsApiFuture.get().getUpdateTime().toString();
    }

    public Tournament getTournament(String tournamentName) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore(); // connect the db
        // we are using email as the documentId. the key in the GET request must be "documentid", and the value is the email
        DocumentReference documentReference = dbFirestore.collection("tournament").document(tournamentName); // get the doc
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
        ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection("tournament").document(tournament.getDocumentId()).set(tournament); // takes name to be primary key
        return collectionsApiFuture.get().getUpdateTime().toString();
    }

    public String deleteTournament(String documentId) throws ExecutionException, InterruptedException {
        // we are using email as the documentId. the key in the DELETE request must be "documentid", and the value is the email
        Firestore dbFirestore = FirestoreClient.getFirestore(); // connect the db
        ApiFuture<WriteResult> writeResult = dbFirestore.collection("tournament").document(documentId).delete(); // get the doc
        return "Successfully deleted " + documentId;
    }

    // // CRUD for Rounds (nested under Tournament)
    // public void addRound(String tournamentId, Round round) {
    //     CollectionReference rounds = db.collection("tournaments").document(tournamentId).collection("rounds");
    //     rounds.document(round.getDocumentId()).set(round);
    // }

    // public List<Round> getRounds(String tournamentId) throws Exception {
    //     return db.collection("tournaments").document(tournamentId).collection("rounds")
    //              .get().get().toObjects(Round.class);
    // }

    // // CRUD for Matches (nested under Round -> Tournament)
    // public void addMatch(String tournamentId, String roundId, Match match) {
    //     CollectionReference matches = db.collection("tournaments").document(tournamentId)
    //                                     .collection("rounds").document(roundId).collection("matches");
    //     matches.document(match.getDocumentId()).set(match);
    // }

    // public List<Match> getMatches(String tournamentId, String roundId) throws Exception {
    //     return db.collection("tournaments").document(tournamentId)
    //              .collection("rounds").document(roundId).collection("matches")
    //              .get().get().toObjects(Match.class);
    // }

    // // CRUD for Standings (nested under Round -> Tournament)
    // public void addStanding(String tournamentId, String roundId, Standings standing) {
    //     CollectionReference standings = db.collection("tournaments").document(tournamentId)
    //                                       .collection("rounds").document(roundId).collection("standings");
    //     standings.document(standing.getDocumentId()).set(standing);
    // }

    // public List<Standings> getStandings(String tournamentId, String roundId) throws Exception {
    //     return db.collection("tournaments").document(tournamentId)
    //              .collection("rounds").document(roundId).collection("standings")
    //              .get().get().toObjects(Standings.class);
    // }
}
