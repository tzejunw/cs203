package com.java.firebase.demo.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.FirestoreClient;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication(scanBasePackages = {"com.java.firebase.demo","com.java.firebase.demo.user","com.java.firebase.demo.tournament"})
@Configuration
public class FirebaseConfig {
    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        // Load environment variables
        Dotenv dotenv = Dotenv.load();

        // Get the Base64 encoded key from environment variable
        String keyBase64 = dotenv.get("FIREBASE_SERVICE_ACCOUNT_KEY_BASE64");

        // Decode the Base64 key
        byte[] decodedKey = java.util.Base64.getDecoder().decode(keyBase64);

        // Create InputStream from the decoded bytes
        ByteArrayInputStream serviceAccount = new ByteArrayInputStream(decodedKey);

        // Set Firebase options
        FirebaseOptions options = new FirebaseOptions.Builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .setStorageBucket("cs203-a263b.appspot.com")
            .build();

        // Initialize and return FirebaseApp
        if (FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.initializeApp(options);
        } else {
            return FirebaseApp.getInstance();
        }
    }
    
    @Bean
    public Firestore getFirestore(FirebaseApp firebaseApp) {
        return FirestoreClient.getFirestore(firebaseApp);
    }

    @Bean
    public FirebaseAuth getFirebaseAuth() {
        return FirebaseAuth.getInstance();
    }
}
