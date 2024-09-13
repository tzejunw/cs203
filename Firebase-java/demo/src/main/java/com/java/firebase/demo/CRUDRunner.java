package com.java.firebase.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import io.github.cdimascio.dotenv.Dotenv;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;


// some boiler plate to authenticate and connect to firebase

@SpringBootApplication(scanBasePackages = {"com.java.firebase.demo","com.java.firebase.demo.user","com.java.firebase.demo.tournament"})
public class CRUDRunner {

    public static void main(String[] args) throws IOException {
        // Load environment variables
        Dotenv dotenv = Dotenv.load();

        // Get the Base64 encoded key from environment variable
        String keyBase64 = dotenv.get("FIREBASE_SERVICE_ACCOUNT_KEY_BASE64");

        // Decode the Base64 key
        byte[] decodedKey = java.util.Base64.getDecoder().decode(keyBase64);

        // Create InputStream from the decoded bytes
        ByteArrayInputStream serviceAccount = new ByteArrayInputStream(decodedKey);

        FirebaseOptions options = new FirebaseOptions.Builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build();

        FirebaseApp.initializeApp(options);

        SpringApplication.run(CRUDRunner.class, args);
    }
}




// @SpringBootApplication
// public class CRUDRunner {

// 	public static void main(String[] args) throws IOException {
// 		ClassLoader classLoader = CRUDRunner.class.getClassLoader(); 

// 		File file = new File(Objects.requireNonNull(classLoader.getResource("serviceAccountKey.json")).getFile());
// 		FileInputStream serviceAccount = new FileInputStream(file.getAbsolutePath());

				
// 		FirebaseOptions options = new FirebaseOptions.Builder()
// 			.setCredentials(GoogleCredentials.fromStream(serviceAccount))
// 			.build();
				
// 		FirebaseApp.initializeApp(options);
				

// 		SpringApplication.run(CRUDRunner.class, args);
// 	}

// }
