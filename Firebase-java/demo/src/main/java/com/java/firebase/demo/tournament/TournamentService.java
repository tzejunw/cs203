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
    // This takes one of the specified json fields, here .getTournamentName(),  and sets it as the documentId (document identifier)
    // if you want firebase to generate documentId for us, leave .document() blank
    public String createTournament(Tournament tournament) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore(); 
        ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection("tournament").document(tournament.getTournamentName()).set(tournament);
        
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


    // // CRUD for Matches (nested under Round -> Tournament)


    // // CRUD for Standings (nested under Round -> Tournament)

}
