package com.java.firebase.demo.user;

import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;

@Service
public class UserService {
    public String createUser(User user) throws ExecutionException, InterruptedException{
        Firestore dbFirestore = FirestoreClient.getFirestore(); 
        // documentId (identifier)is based on what we gave, .getEmail(). 
        //if you want firebase to generate for us, leave .document() blank
        // if you submit another create req with the same "documentId" now, it will overwrite
        ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection("user").document(user.getEmail()).set(user); 
        
        return collectionsApiFuture.get().getUpdateTime().toString();
    }


    public User getUser(String email) throws ExecutionException, InterruptedException{
        Firestore dbFirestore = FirestoreClient.getFirestore(); // connect the db
        // we are using email as the documentId. the key in the GET request must be "documentid", and the value is the email
        DocumentReference documentReference = dbFirestore.collection("user").document(email); // get the doc
        ApiFuture<DocumentSnapshot> future = documentReference.get();
        DocumentSnapshot document = future.get();
        User user;
        if (document.exists()) {
            user = document.toObject(User.class); // convert to object
            return user;
        }
        return null;
    }
    // this is exact same as POST route!! should we input validate?
    // TODO input validate that the json has "documentId" and that its value pair exisits in the database!
    public String updateUser(User user) throws ExecutionException, InterruptedException{ 
        Firestore dbFirestore = FirestoreClient.getFirestore(); // connect the db
        ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection("user").document(user.getDocumentId()).set(user); // takes name to be primary key
        return collectionsApiFuture.get().getUpdateTime().toString();
    }

    public String deleteUser(String documentId) throws ExecutionException, InterruptedException {
        // we are using email as the documentId. the key in the DELETE request must be "documentid", and the value is the email
        Firestore dbFirestore = FirestoreClient.getFirestore(); // connect the db
        ApiFuture<WriteResult> writeResult = dbFirestore.collection("user").document(documentId).delete(); // get the doc
        return "Successfully deleted " + documentId;
    }

}

