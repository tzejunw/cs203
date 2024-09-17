package com.java.firebase.demo.user;

import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.cloud.FirestoreClient;

@Service
public class UserService {
    // This takes one of the specified json fields, here .getEmail(), and sets it as
    // the documentId (document identifier) if you want firebase to generate documentId for us, leave .document() blank
    public String createUser(Register register) throws ExecutionException, InterruptedException {
        // Step 1: Validate if registration details fits our business requirements
        // Email validations is done by Firebase Auth
        if (!isPasswordValid(register.getPassword()))
            return "Error: Password should be between 8-32 characters, at least 1 uppercase and lowercase letter, 1 digit and 1 special character.";
        if (!isBirthdayValid(register.getBirthday()))
            return "Error: Incorrect birthday format.";
        if (!isUsernameValid(register.getUserName()))
            return "Error: Username should be between 3-32 characters long";
        if (!isUsernameUnique(register.getUserName()))
            return "Error: Username exists, please choose another username";
        
        // Step 2: Create an account in Firebase Authentication
        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(register.getEmail())
                .setPassword(register.getPassword())
                .setEmailVerified(false);
                // .setDisabled(false);

        UserRecord userRecord;
        try {
            userRecord = FirebaseAuth.getInstance().createUser(request);
        } catch (FirebaseAuthException e) {
            return "Account creation failed: " + e.getMessage();
        }

        // Step 3: Store user data in Firestore once the user record is created
        // Need to recreate user class to exclude storing password into firestore ><
        // Player status is set here too, to avoid people from manipulating the data to become an admin.
        User user = new User();
        user.setUserName(register.getUserName());
        user.setName(register.getName());
        user.setBirthday(register.getBirthday());
        user.setUserStatus("player");
        user.setEmail(register.getEmail());
        user.setGender(register.getGender());

        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection("user").document(userRecord.getUid())
                .set(user);

        try {
            // Wait for the Firestore write operation to complete
            WriteResult writeResult = collectionsApiFuture.get();
            return "User data successfully written at: " + writeResult.getUpdateTime();
        } catch (Exception e) {
            return "Failed to store user data: " + e.getMessage();
        }

        // Firestore dbFirestore = FirestoreClient.getFirestore();
        // ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection("user").document(user.getEmail()).set(user);

        // return collectionsApiFuture.get().getUpdateTime().toString();
    }

    // Verify the JWT (Firebase ID token) and decode it
    public String getIdToken(String bearerToken) throws FirebaseAuthException {
        try {
            // Extract the token (assuming it's in the format "Bearer <JWT>")
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                throw new IllegalArgumentException("Invalid bearer token");
            }
            String idToken = bearerToken.substring(7);  // Remove "Bearer " from the header

            // Verify the token and get the user's UID
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            return decodedToken.getUid();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public FirebaseToken verifyIdToken(String idToken) throws FirebaseAuthException {
        try {
            return FirebaseAuth.getInstance().verifyIdToken(idToken);
        } catch (FirebaseAuthException e) {
            throw e;
        }
    }

    private static final String FIREBASE_API_KEY = "AIzaSyBItH-UkQG9U1UfRILfioF7K_VeEw_Zbjo"; 

    // Retrieves the idToken on login (email & password) 
    // @TODO: store it as users' cookies on spring boot (for internal testing) & frontend.
    public String login(Login login) throws ExecutionException, InterruptedException, JsonProcessingException {
        // Creates a JSON template to send to Firebase Auth Client
        String requestPayload = String.format("{\"email\":\"%s\",\"password\":\"%s\",\"returnSecureToken\":true}", login.getEmail(), login.getPassword());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create the request entity
        HttpEntity<String> entity = new HttpEntity<>(requestPayload, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + FIREBASE_API_KEY, 
                HttpMethod.POST, 
                entity, 
                String.class
        );

        // Parse the response
        if (response.getStatusCode() == HttpStatus.OK) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonResponse;
            try {
                jsonResponse = mapper.readTree(response.getBody());
                String idToken = "Token: " + jsonResponse.get("idToken").asText();
                return idToken;
            } catch (JsonMappingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } 
            return "JSON creation error";
        } 
        if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
            return "Invalid_login_credential";
        }
        return "Failed to authenticate user";
    }

    // Token expires every hour, hence we need to refresh it when it expires.
    public String refreshToken(String refreshToken) throws Exception {
        // Prepare the request payload
        String requestPayload = String.format("{\"grant_type\":\"refresh_token\",\"refresh_token\":\"%s\"}", refreshToken);

        // Create the headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create the request entity
        HttpEntity<String> entity = new HttpEntity<>(requestPayload, headers);

        // Make the HTTP request using RestTemplate
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                "https://securetoken.googleapis.com/v1/token?key=" + FIREBASE_API_KEY,
                HttpMethod.POST,
                entity,
                String.class
        );

        // Parse the response
        if (response.getStatusCode() == HttpStatus.OK) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonResponse = mapper.readTree(response.getBody());
            String newIdToken = jsonResponse.get("id_token").asText();
            return newIdToken;
        } else {
            throw new Exception("Failed to refresh token");
        }
    }

    // For this Firebase doc, the uid is the documentId.
    public User getUser(String uid) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore(); // connect the db
        DocumentReference documentReference = dbFirestore.collection("user").document(uid); // get the doc
        ApiFuture<DocumentSnapshot> future = documentReference.get();
        DocumentSnapshot document = future.get();
        User user;
        if (document.exists()) {
            user = document.toObject(User.class); // convert to object
            return user;
        }
        return null;
    }

    public String getUserEmail(String uid) throws ExecutionException, InterruptedException, FirebaseAuthException {
        UserRecord userRecord = FirebaseAuth.getInstance().getUser(uid);
        return userRecord.getEmail();
    }

    // this is exact same as POST route!! should we input validate?
    // @TODO: Disable setting of userStatus
    // exisits in the database!
    public String updateUser(User user, String uid) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore(); // connect the db
        ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection("user").document(uid).set(user);
        return collectionsApiFuture.get().getUpdateTime().toString();
    }

    // For this Firebase doc, the uid is the documentId.
    public String deleteUser(String uid) throws ExecutionException, InterruptedException, FirebaseAuthException {
        FirebaseAuth.getInstance().deleteUser(uid);
        Firestore dbFirestore = FirestoreClient.getFirestore(); // connect the db
        ApiFuture<WriteResult> writeResult = dbFirestore.collection("user").document(uid).delete();
        return "Successfully deleted " + uid;
    }

    // Birthday date format checks DD/MM/YYYY
    // @TODO
    public static boolean isBirthdayValid(String birthday) {
        return true;
    }

    // Password length & complexity check
    private static final int PASSWORD_MIN_LENGTH = 8;
    private static final int PASSWORD_MAX_LENGTH = 32;
    private static final Pattern UPPER_CASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWER_CASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR = Pattern.compile("[^a-zA-Z0-9]");

    public static boolean isPasswordValid(String password) {
        if (password.length() < PASSWORD_MIN_LENGTH || password.length() > PASSWORD_MAX_LENGTH) {
            return false;
        }
        if (!UPPER_CASE.matcher(password).find()) {
            return false;
        }
        if (!LOWER_CASE.matcher(password).find()) {
            return false;
        }
        if (!DIGIT.matcher(password).find()) {
            return false;
        }
        if (!SPECIAL_CHAR.matcher(password).find()) {
            return false;
        }
        return true;
    }

    // Username checks
    private static final int USERNAME_MIN_LENGTH = 3;
    private static final int USERNAME_MAX_LENGTH = 32;
    public static boolean isUsernameValid(String username) {
        if (username.length() < USERNAME_MIN_LENGTH || username.length() > USERNAME_MAX_LENGTH) {
            return false;
        }
        return true;
    }
    // TODO: Scan through the entire DB for the same username
    // If no username exists, return true.
    public static boolean isUsernameUnique(String username) {
        return true;
    }
}
