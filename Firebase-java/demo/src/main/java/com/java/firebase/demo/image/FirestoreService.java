package com.java.firebase.demo.image;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;

@Service
public class FirestoreService {

    private final Firestore firestore;

    // Constructor to initialize Firestore instance
    public FirestoreService(Firestore firestore) {
        // Make sure Firestore is initialized properly
        this.firestore = firestore;
    }

    // Save the image URL to Firestore under the specified document
    public void saveImageUrl(String documentId, String imageUrl) {
        try {
            firestore.collection("tournament")
                    .document(documentId)
                    .update("imageUrl", imageUrl)
                    .get(); // Use .get() to ensure the update completes
        } catch (Exception e) {
            // Handle any exceptions during Firestore operations
            throw new RuntimeException("Error saving image URL to Firestore", e);
        }
    }
}

