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
    // This takes one of the specified json fields, here .getEmail(),  and sets it as the documentId (document identifier)
    // if you want firebase to generate documentId for us, leave .document() blank
    public String createUser(User user) throws ExecutionException, InterruptedException{
        Firestore dbFirestore = FirestoreClient.getFirestore(); 
        ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection("user").document(user.getEmail()).set(user); 
        
        return collectionsApiFuture.get().getUpdateTime().toString();
    }

    // For this Firebase doc, the email is the documentId. 
    // The key in the GET request must be "documentid", and the value is the email
    public User getUser(String documentId) throws ExecutionException, InterruptedException{
        Firestore dbFirestore = FirestoreClient.getFirestore(); // connect the db
        DocumentReference documentReference = dbFirestore.collection("user").document(documentId); // get the doc
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
        ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection("user").document(user.getEmail()).set(user);
        return collectionsApiFuture.get().getUpdateTime().toString();
    }

    // For this Firebase doc, the email is the documentId. 
    // The key in the GET request must be "documentid", and the value is the email
    public String deleteUser(String documentId) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore(); // connect the db
        ApiFuture<WriteResult> writeResult = dbFirestore.collection("user").document(documentId).delete();
        return "Successfully deleted " + documentId;
    }

}

