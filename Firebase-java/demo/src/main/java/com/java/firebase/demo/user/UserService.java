package com.java.firebase.demo.user;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreException;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.common.base.Strings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.UpdateRequest;
import com.google.firebase.cloud.FirestoreClient;
import com.java.firebase.demo.Exceptions.TooManyRequestsException;

import io.github.cdimascio.dotenv.Dotenv;


@Service
public class UserService {
    private final Firestore firestore;
    private final FirebaseAuth firebaseAuth;

    public UserService(Firestore firestore, FirebaseAuth firebaseAuth) {
        this.firestore = firestore;
        this.firebaseAuth = firebaseAuth;
    }

    /**
         * Creates a new user account in Firebase Authentication. 
         * Main connection with UserController.
         * @param email The user's email address.
         * @param password The user's password.
         * @return The UID of the newly created user.
    */
    public String createUser(UserCredentials userCredentials) throws ExecutionException, InterruptedException, FirebaseAuthException{
        validateCredentials(userCredentials.getEmail(), userCredentials.getPassword());
        
        String userId = registerUserInFirebaseAuth(userCredentials.getEmail(), userCredentials.getPassword());
        
        // Send email verification to the user
        sendVerificationEmail(userCredentials.getEmail());

        // setAdminAuthority(userId); // Uncomment to make the next registration an admin user.
        return userId;
    }

    // Create an account in Firebase Authentication
    // Returns Unique ID (uid)
    public String registerUserInFirebaseAuth(String email, String password) throws ExecutionException, InterruptedException, FirebaseAuthException {
        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(email)
                .setPassword(password)
                .setEmailVerified(false);
        UserRecord userAuthRecord = firebaseAuth.createUser(request);
        return userAuthRecord.getUid();
    }

    /**
     * Sets the "admin" custom claim for the specified user.
     * @param userId The UID of the user.
    */
    public void setAdminAuthority(String userId) throws ExecutionException, InterruptedException, FirebaseAuthException {
        Map<String, Object> claims = new HashMap<>();
        final String ROLE_ADMIN = "admin";
        claims.put(ROLE_ADMIN, true);
        firebaseAuth.setCustomUserClaims(userId, claims);
    }

    /**
     * Creates a new document in the "user" collection with the specified userId and user data.
     * @param user The user object containing the user's data.
     * @param userId The user's UID.
     */
    public void createUserDetails(User user, String userId)
            throws ExecutionException, InterruptedException, FirebaseAuthException, FirestoreException {
        if (!(user.getGender().equals("Male") || user.getGender().equals("Female")))
            throw new IllegalArgumentException("Gender must be 'Male' or 'Female'");
        if (!isBirthdayValid(user.getBirthday()))
            throw new IllegalArgumentException("Incorrect birthday format, format should be DD/MM/YYYY");
        if (!isUsernameValid(user.getUserName()))
            throw new IllegalArgumentException("Username should be between 3-32 characters long");
        if (!isUsernameUnique(user.getUserName()))
            throw new IllegalArgumentException("Username exists, please choose another username");
        
        firestore.collection("user").document(userId).set(user);
    }

    // Method to send email verification
    public String sendVerificationEmail(String email) throws FirebaseAuthException {
        // Trigger the email verification
        String verificationLink = firebaseAuth.generateEmailVerificationLink(email);

        System.out.println(verificationLink);

        // Create an instance of EmailService
        EmailService emailService = new EmailService();

        // Use the EmailService to send the verification email
        emailService.sendVerificationEmail(email, verificationLink);
        
        return verificationLink;
    }
    
    // Method to mock verifying of email processed on firebase.
    public void verifyUserEmail(String userId) throws FirebaseAuthException {
        UpdateRequest request = new UpdateRequest(userId).setEmailVerified(true);
        firebaseAuth.updateUser(request);
    }

    // Verify the Firebase ID token and decode it
    public String getIdToken(String bearerToken) throws FirebaseAuthException {
        try {
            // Extract the token (assuming it's in the format "Bearer <JWT>")
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                throw new IllegalArgumentException("Invalid bearer token");
            }
            String idToken = bearerToken.substring(7); // Remove "Bearer " from the header

            // Verify the token with firebase auth and get the user's UID
            FirebaseToken decodedToken = verifyIdToken(idToken);
            return decodedToken.getUid();
        } catch (Exception e) {
            throw e;
        }
    }

    public FirebaseToken verifyIdToken(String idToken) throws FirebaseAuthException {
        return firebaseAuth.verifyIdToken(idToken);
    }

    Dotenv dotenv = Dotenv.load();
    private final String FIREBASE_API_KEY = dotenv.get("FIREBASE_API_KEY");

    /**
     * Parses a JSON response and extracts the specified field.
     * @param response The HTTP response containing the JSON data.
     * @param fieldName The name of the field to extract.
     * @return The value of the specified field, or null if the field does not exist.
    */
    public String parseJSON(ResponseEntity<String> response, String fieldName) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonResponse = mapper.readTree(response.getBody());
        String field = jsonResponse.get(fieldName).asText();
        return field;
    }

    /**
     * Verifies the ID token and retrieves the user record to check if email is verified.
     * @param idToken The ID token to verify.
     * @return The user record associated with the ID token.
    */
    public boolean isEmailVerified(String idToken) throws Exception {
        FirebaseToken decodedToken = verifyIdToken(idToken);
        UserRecord userRecord = firebaseAuth.getUser(decodedToken.getUid());
        return userRecord.isEmailVerified();
    }

    /**
     * Validates the user credentials and ensures the email is verified before granting access.
     * @param email The user's email address.
     * @param password The user's password.
    */
    public String login(UserCredentials userCredentials)
        throws ExecutionException, InterruptedException, JsonProcessingException, Exception {
        validateCredentials(userCredentials.getEmail(), userCredentials.getPassword());
            
        // Sets the content type header to application/json, as well as it's required variables.
        String requestPayload = String.format("{\"email\":\"%s\",\"password\":\"%s\",\"returnSecureToken\":true}", userCredentials.getEmail(), userCredentials.getPassword());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Creates an HTTP entity containing the request payload and headers
        HttpEntity<String> entity = new HttpEntity<>(requestPayload, headers);
        
        String token = sendLoginRequest(entity);

        if (!isEmailVerified(token)) {
            System.out.println("Email not verified");
            throw new IllegalArgumentException("Please verify your account via your email to continue.");
        }

        return token;
    }

    private String sendLoginRequest(HttpEntity<String> entity) throws Exception {
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + FIREBASE_API_KEY,
                    HttpMethod.POST,
                    entity,
                    String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return parseJSON(response, "idToken");
            }

            throw new Exception("Something went wrong");
        } catch (HttpClientErrorException e) {
            if (e.getMessage().contains("too-many-requests")) {
                throw new TooManyRequestsException("Too many requests detected. Please try again later.");
            }
            if (e.getStatusCode() == HttpStatus.FORBIDDEN && e.getMessage().contains("blocked")) {
                throw new AccessDeniedException(e.getMessage());
            }
            if (e.getMessage().contains("USER_DISABLED")){
                throw new IllegalArgumentException("This account has been suspended. Please contact our customer service for more info.");
            }
            throw new IllegalArgumentException("Email or password is incorrect.");
        } catch (HttpServerErrorException e) {
            throw new HttpServerErrorException(e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * Revokes all refresh tokens associated with the specified user.
     * @param userId The user's UID.
    */
    public void logoutUser(String userId) throws FirebaseAuthException {
        firebaseAuth.revokeRefreshTokens(userId);
    }

    /**
     * Checks if a user with the specified UID exists in the Firestore database.
     * @param userId The user's UID.
     * @return True if the user exists, false otherwise.
    */
    public Boolean userExists(String userId) throws ExecutionException, InterruptedException {
        DocumentReference documentReference = firestore.collection("user").document(userId);
        ApiFuture<DocumentSnapshot> future = documentReference.get();
        DocumentSnapshot document = future.get();
        return document.exists();
    }

    /**
     * Retrieves a user from the Firestore database by their UID.
     * @param userId The user's UID.
     * @return The user object, or null if the user does not exist.
    */
    public User getUser(String userId) throws ExecutionException, InterruptedException, Exception {
        DocumentReference documentReference = firestore.collection("user").document(userId); // get the doc
        ApiFuture<DocumentSnapshot> future = documentReference.get();
        DocumentSnapshot document = future.get();
        User user;
        if (document.exists()) {
            user = document.toObject(User.class); // convert to object
            return user;
        }
        throw new IllegalArgumentException("User not found.");
    }

    public String getUserName(String userId) throws ExecutionException, InterruptedException, Exception {
        User user = getUser(userId);
        return user.getUserName();
    }

    public List<User> getAllUsers() throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        
        // Retrieve all documents from the "user" collection
        ApiFuture<QuerySnapshot> future = dbFirestore.collection("user").get();
        
        // QuerySnapshot contains all documents in the collection
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        
        // Create a list to hold the User objects
        List<User> users = new ArrayList<>();
        
        // Convert each document to a usr object and add it to the list
        for (DocumentSnapshot document : documents) {
            User user = document.toObject(User.class);
            users.add(user);
        }
        
        return users;
    }
    
    public User getPlayer(String userName) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        
        // Query the "user" collection to find the document where the userName matches
        ApiFuture<QuerySnapshot> future = dbFirestore.collection("user")
                                                      .whereEqualTo("userName", userName)
                                                      .get();
        
        // Get the list of matching documents (should contain at most one result)
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        
        // Check if any document was found
        if (!documents.isEmpty()) {
            // Convert the first matching document to a User object
            return documents.get(0).toObject(User.class);
        } else {
            // Return null or handle the case where no user was found
            return null;
        }
    }
    

    // public String getUserEmail(String userId) throws ExecutionException, InterruptedException, FirebaseAuthException {
    //     UserRecord userRecord = firebaseAuth.getUser(userId);
    //     return userRecord.getEmail();
    // }

    // User only allowed to update gender, birthday and name 
    public String updateUser(User user, String userId) throws ExecutionException, InterruptedException {
        if (!(user.getGender().equals("Male") || user.getGender().equals("Female")))
            throw new IllegalArgumentException("Gender must be 'Male' or 'Female'");
        if (!isBirthdayValid(user.getBirthday()))
            throw new IllegalArgumentException("Incorrect birthday format, format should be DD/MM/YYYY");
        
        if (userExists(userId)) {
            ApiFuture<WriteResult> collectionsApiFuture = firestore.collection("user").document(userId).set(user);
            return collectionsApiFuture.get().getUpdateTime().toString();
        }
        throw new IllegalArgumentException("User not found.");
    }

    public String updatePassword(String newPassword, String userId)
            throws ExecutionException, InterruptedException, FirebaseAuthException {
        if (!isPasswordValid(newPassword))
            throw new IllegalArgumentException(
                    "Password should be between 8-32 characters, at least 1 uppercase, 1 lowercase letter, 1 digit and 1 special character.");
        UpdateRequest request = new UserRecord.UpdateRequest(userId).setPassword(newPassword);
        firebaseAuth.updateUser(request);
        return "Successfully updated password";
    }

    public String deleteUser(String userId) throws ExecutionException, InterruptedException, FirebaseAuthException {
        firebaseAuth.deleteUser(userId);
        firestore.collection("user").document(userId).delete();
        return "Successfully deleted " + userId;
    }

    // Birthday date format checks DD/MM/YYYY
    public boolean isBirthdayValid(String birthday) {
        String regex = "^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[0-2])/\\d{4}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher birthdayMatcher = pattern.matcher(birthday);
        return birthdayMatcher.matches();
    }

    // Password length & complexity check
    private static final int PASSWORD_MIN_LENGTH = 8;
    private static final int PASSWORD_MAX_LENGTH = 32;
    private static final Pattern UPPER_CASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWER_CASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR = Pattern.compile("[^a-zA-Z0-9]");

    public boolean isPasswordValid(String password) {
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

    private void validateCredentials(String email, String password) {
        if (Strings.isNullOrEmpty(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (Strings.isNullOrEmpty(password)) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (!isPasswordValid(password)){
            throw new IllegalArgumentException("Password should be between 8-32 characters, at least 1 uppercase, 1 lowercase letter, 1 digit and 1 special character.");
        }
    }

    // Username checks
    private static final int USERNAME_MIN_LENGTH = 3;
    private static final int USERNAME_MAX_LENGTH = 32;

    public boolean isUsernameValid(String username) {
        if (username.length() < USERNAME_MIN_LENGTH || username.length() > USERNAME_MAX_LENGTH) {
            return false;
        }
        return true;
    }

    // Checks if username exists.
    public boolean isUsernameUnique(String username) throws ExecutionException, InterruptedException {
        String normalizedUsername = username.toLowerCase();

        ApiFuture<QuerySnapshot> future = firestore.collection("user")
                .whereEqualTo("userName", normalizedUsername)
                .get();

        QuerySnapshot querySnapshot = future.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

        return documents.isEmpty();
    }
}
