package com.java.firebase.demo;
// talk to firebase with logic

import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;

@Service
public class CRUDService {
    public String createCRUD(CRUD crud) throws ExecutionException, InterruptedException{
        Firestore dbFirestore = FirestoreClient.getFirestore(); 
        // identifier id is based on what we gave, .getDocumentId. if you want firebase to generate for us, leave .document() blank
        // if you submit another create req with the same "documentId" now, it will overwrite
        ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection("user").document(crud.getDocumentId()).set(crud); 
        
        return collectionsApiFuture.get().getUpdateTime().toString();
    }


    public CRUD getCRUD(String documentId) throws ExecutionException, InterruptedException{
        Firestore dbFirestore = FirestoreClient.getFirestore(); // connect the db
        DocumentReference documentReference = dbFirestore.collection("user").document(documentId); // get the doc
        ApiFuture<DocumentSnapshot> future = documentReference.get();
        DocumentSnapshot document = future.get();
        CRUD crud;
        if (document.exists()) {
            crud = document.toObject(CRUD.class); // convert to object
            return crud;
        }
        return null;
    }

    public String updateCRUD(CRUD crud) throws ExecutionException, InterruptedException{
        Firestore dbFirestore = FirestoreClient.getFirestore(); // connect the db
        ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection("user").document(crud.getDocumentId()).set(crud); // takes name to be primary key
        return collectionsApiFuture.get().getUpdateTime().toString();
    }

    public String deleteCRUD(String documentId) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore(); // connect the db
        ApiFuture<WriteResult> writeResult = dbFirestore.collection("user").document(documentId).delete(); // get the doc
        return "Successfully deleted " + documentId;
    }



    
    
}
