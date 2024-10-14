package com.java.firebase.demo;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


// some boiler plate to authenticate and connect to firebase

@SpringBootApplication(scanBasePackages = {"com.java.firebase.demo","com.java.firebase.demo.user","com.java.firebase.demo.tournament"})
public class CRUDRunner {

    public static void main(String[] args) throws IOException {
        // Load environment variables
        // Dotenv dotenv = Dotenv.load();

        // // Get the Base64 encoded key from environment variable
        // String keyBase64 = dotenv.get("FIREBASE_SERVICE_ACCOUNT_KEY_BASE64");

        // // Decode the Base64 key
        // byte[] decodedKey = java.util.Base64.getDecoder().decode(keyBase64);

        // // Create InputStream from the decoded bytes
        // ByteArrayInputStream serviceAccount = new ByteArrayInputStream(decodedKey);

        // FirebaseOptions options = new FirebaseOptions.Builder()
        //     .setCredentials(GoogleCredentials.fromStream(serviceAccount))
        //     .setStorageBucket("cs203-a263b.appspot.com")
        //     .build();

        // FirebaseApp.initializeApp(options);

        SpringApplication.run(CRUDRunner.class, args);
    }
}