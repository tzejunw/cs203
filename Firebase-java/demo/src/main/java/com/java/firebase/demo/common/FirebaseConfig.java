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

@SpringBootApplication(scanBasePackages = {"com.java.firebase.demo","com.java.firebase.demo.user","com.java.firebase.demo.tournament"})
@Configuration
public class FirebaseConfig {
    // @Value("${FIREBASE_SERVICE_ACCOUNT_KEY_BASE64}")
    private String keyBase64 = "ewogICJ0eXBlIjogInNlcnZpY2VfYWNjb3VudCIsCiAgInByb2plY3RfaWQiOiAiY3MyMDMtYTI2M2IiLAogICJwcml2YXRlX2tleV9pZCI6ICJhMWMzNDQwNTdjYzI5Yzk0NTYyMDU1ODJhYjFmMGExMjBlZTVmNzI5IiwKICAicHJpdmF0ZV9rZXkiOiAiLS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tXG5NSUlFdlFJQkFEQU5CZ2txaGtpRzl3MEJBUUVGQUFTQ0JLY3dnZ1NqQWdFQUFvSUJBUUQ0TjlueXAxNnI4TnZUXG5mcHNNQWwwejhaa3R6cUljWFprNTRsNHFDcTVjNmo0NDNpSWNSeUhQY3djaW00MVFwWlNhc2J3ZkdxVFJKTncyXG5vcFF3SnlZR3h2QmV5b042bG9ZLzV2MVJCbnQyOERGbDdiekVwanNsU3VtdkZ4L0Vyb1lpYnltTUNpanFDaHBpXG5ENEE1OHMzdndZUjhMYmswVlRMakVJS1MwK2lWN0cyWHlTbnE5cnlLNmV0SWZPenBUd0dOWUNDTEhzM1lDT0c0XG4vMEVvWG5CZlkvOXY4T281TEJ3WGxlSkkzcGpEcGU1S3U4ZFBqVk4zTkpCZ3k3WVg1alJaT3E3TDBObXNFZjhBXG4veXhpM2xUM1RKQW00MmFiZmxLK0ZJMUwxaTRjdVFzSkdDV3NWdUJEWm9sL1JUY242SEVJZG9FUU4vSmhSVnFZXG5hNTZDTHZnckFnTUJBQUVDZ2dFQWVkaVk0WGZ0amtQbTlUUWlFa3dheW9QZnFNL1ZYazFJQ1ZVbzJrUnlBV1J2XG5kT1QrSTVsN2JHVlA3Rm9samFBcWlhR1Q4a1RYQVYreHhoUWE0R1JuVTA1VzhvRkE5MjFxcTNYTmhmZkRwY2dVXG54NjlqU2JrOE1YdExxVXFWcXF2dFpudmhEa0J5Z3lqQWVNQ1pObGtOWkpNL1lPSGxGMTUzWTJWWVk1RW9qR2NTXG5qSnpOUnl4MkNUMnZraHBlZlZQRWRvTFh4MVE2aUdxZm5qZVRVeGtPRGdyQUhHMFhTTXZaMUo4dlJLNnEzME1ZXG5hM3hQWFozOGc4cy9OMUlxSVQ5S0xvRlRVMWpmaDRyU0l2WG1ldEk2K2I5M3NXbTA2TEF5dFFXNnhnN1p3b3NDXG5ZVmt2bnNvVXNRQ1NiTFdoZlp5WE52d1dMeHZ4TjhORi9uOXNoclQvNFFLQmdRRDhpejMzRWRRSmRjL2ZVdzUzXG5nME9iMXNKSDYxWkUydXdlYlowT1BhVEl4OFJLcFFzczZGNHdoeGQrM3ozd2JEWHpLcVloSEJzaWxpcTdndVQyXG5ac1lNa1R4Q3hVdjl2RDIwdUk0ZlNNek4rMVBjbXJDNEhnOVo3N2V4SSt0UjdKTnpqeXFVdktyRG9aZnp0ZWpLXG5obks4RlhFS1ZsVm9HbmtzUjY3YmxseThFUUtCZ1FEN25YUmVsek96OTlRUjlkaUMyV0xYL1F1NGdhUHVpM3g5XG50U1VXMEUycG10RDhWREd5S1AvLzRsSUFuUmJPU0lxUnhZdnFHRUNua3hTMWxObEQ1dkJOdlhJMmF5WE0wb0RQXG5rb3llempJNzcvM3hHNDZFSzBGNTBhVk1hL1R6bnFWdDN5cWR2WEJyUWx6bXRET2Y4RWNvTDFZczFOZ0dsU2l1XG5PY2JWMitMY2V3S0JnQU1aSWpWNXZvUXZNZEduanpIZ0ptQXZEa3Niay85Y1FERlBYdkhoeDdlKzI2V2V5TGhRXG5VWGQrOE1iWnJrVFl6d0Nnc1ZFT3F6YU5hUkRMMWtzL2o0WnY4YkZRQWxLbWtJczdDR05SM1ZGWmwvbkkzQW9tXG5ORUlDTXRxMWVobVRNV3ZsZ2J3NVpFN3FHSmNnMEM1TDMvUjJ2dUJGbHQ2a0ZJQjZrS2FZL2FKQkFvR0FmZmVFXG5ETjhSeVdXblRCNlNlRit3dG9VKzJ1NDhUZTZUTXJQL2ozNVZnelc0cnJyUDdtdk1UaVRWL095b1FEbmM5Y0c2XG5uRVhzQ0hrQUF6QXozV1MrcFl6VW94M2RYTDkrTVcwaTdWWTVtL3c0Vy83NXlIMkhXQjkyNjkyWVVtYjRWeHRFXG4wSmZJc2tvVkRJTG0zWW1EZGtETW1jRXRMWTU3UVU0M2wxVndMQVVDZ1lFQXJ3MTFpYyt3MUZBN2E4U3FQODdVXG5oZ0UxcGg2MEZlUDNKK2tmQ2JXT2dGbXFuSk1xYlV6aEhBN0xDU2Q2Nmw4YTVtQlM3YUx1ellvYnRMRmgxRFhkXG5YTXBOQ3BHOWlyWkpaanhiVFZKQ1BMbzlRbFYxMFo3cmJxOHFUNjJBRTZTakpXbDFoNTBqTGM2TTZnUTZ5S3hZXG5BMlpucUh5QUNEeUZlbUlMQTRnMWpuWT1cbi0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS1cbiIsCiAgImNsaWVudF9lbWFpbCI6ICJmaXJlYmFzZS1hZG1pbnNkay02cWVqOEBjczIwMy1hMjYzYi5pYW0uZ3NlcnZpY2VhY2NvdW50LmNvbSIsCiAgImNsaWVudF9pZCI6ICIxMDAyODIzNjI2MTE4NTQzNDY0MzUiLAogICJhdXRoX3VyaSI6ICJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20vby9vYXV0aDIvYXV0aCIsCiAgInRva2VuX3VyaSI6ICJodHRwczovL29hdXRoMi5nb29nbGVhcGlzLmNvbS90b2tlbiIsCiAgImF1dGhfcHJvdmlkZXJfeDUwOV9jZXJ0X3VybCI6ICJodHRwczovL3d3dy5nb29nbGVhcGlzLmNvbS9vYXV0aDIvdjEvY2VydHMiLAogICJjbGllbnRfeDUwOV9jZXJ0X3VybCI6ICJodHRwczovL3d3dy5nb29nbGVhcGlzLmNvbS9yb2JvdC92MS9tZXRhZGF0YS94NTA5L2ZpcmViYXNlLWFkbWluc2RrLTZxZWo4JTQwY3MyMDMtYTI2M2IuaWFtLmdzZXJ2aWNlYWNjb3VudC5jb20iLAogICJ1bml2ZXJzZV9kb21haW4iOiAiZ29vZ2xlYXBpcy5jb20iCn0K";

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        

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
