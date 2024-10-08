package com.java.firebase.demo.user;

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
import org.springframework.security.access.AccessDeniedException;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.UpdateRequest;
import com.google.firebase.cloud.FirestoreClient;
import com.java.firebase.demo.user.Exceptions.TooManyRequestsException;

@Service
public class UserService {
    public void createRecordInFirestore(User user, String uid) throws ExecutionException, InterruptedException, FirestoreException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        dbFirestore.collection("user").document(uid).set(user);
    }

    public String createAccountInAuth(String email, String password) throws ExecutionException, InterruptedException, FirebaseAuthException {
        // Create an account in Firebase Authentication
        // Returns Unique ID (uid)
        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(email)
                .setPassword(password)
                .setEmailVerified(false);
        UserRecord userAuthRecord = FirebaseAuth.getInstance().createUser(request);
        return userAuthRecord.getUid();
    }

    public void setAdminAuthority(String uid) throws ExecutionException, InterruptedException, FirebaseAuthException {
        Map<String, Object> claims = new HashMap<>();
        claims.put("admin", true);
        // claims.put("player", true);
        FirebaseAuth.getInstance().setCustomUserClaims(uid, claims);
    }

    public String createUser(Register register) throws ExecutionException, InterruptedException, FirebaseAuthException{
        if (!isPasswordValid(register.getPassword()))
            throw new IllegalArgumentException("Password should be between 8-32 characters, at least 1 uppercase, 1 lowercase letter, 1 digit and 1 special character.");
        
        String uid = createAccountInAuth(register.getEmail(), register.getPassword());
        
        // Send email verification to the user
        sendVerificationEmail(uid);

        // setAdminAuthority(uid); // Uncomment to make the next registration an admin user.
        return uid;
    }


    // This takes one of the specified json fields, here .getEmail(), and sets it as
    // the documentId (document identifier) if you want firebase to generate
    // documentId for us, leave .document() blank
    // public String OLDcreateUser(Register register)
    //         throws ExecutionException, InterruptedException, FirebaseAuthException, FirestoreException {
    //     // Step 1: Validate if registration details fits our business requirements
    //     // Email validations is conducted by Firebase Auth
        

        

    //     // Step 3: Set player status
        

        

    //     // Step 4: Store user data in Firestore once the user record is created
    //     // Need to recreate user class to exclude storing password into firestore ><
    //     // Player status is set here too, to avoid people from manipulating the data to
    //     // become an admin.
    //     User user = new User();
    //     user.setUserName(register.getUserName().toLowerCase());
    //     user.setName(register.getName());
    //     user.setBirthday(register.getBirthday());
    //     user.setGender(register.getGender());

    //     createRecordInFirestore(user, userAuthRecord.getUid());
    //     return "Successfully added user.";
    // }

    public void createFirestoreRecord(User user, String uid)
            throws ExecutionException, InterruptedException, FirebaseAuthException, FirestoreException {
        if (!(user.getGender().equals("Male") || user.getGender().equals("Female")))
            throw new IllegalArgumentException("Gender must be 'Male' or 'Female'");
        if (!isBirthdayValid(user.getBirthday()))
            throw new IllegalArgumentException("Incorrect birthday format, format should be DD/MM/YYYY");
        if (!isUsernameValid(user.getUserName()))
            throw new IllegalArgumentException("Username should be between 3-32 characters long");
        if (!isUsernameUnique(user.getUserName()))
            throw new IllegalArgumentException("Username exists, please choose another username");
        
        createRecordInFirestore(user, uid);
    }

    // Method to send email verification
    public void sendVerificationEmail(String uid) {

        try {
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            UserRecord user = firebaseAuth.getUser(uid);

            // Trigger the email verification
            String verificationLink = firebaseAuth.generateEmailVerificationLink(user.getEmail());

            System.out.println(verificationLink);

            // Create an instance of EmailService
            EmailService emailService = new EmailService();

            // Use the EmailService to send the verification email
            emailService.sendVerificationEmail(user.getEmail(), verificationLink);

        } catch (FirebaseAuthException e) {
            // Handle exceptions if sending the email fails
            e.printStackTrace();
        }
    }

    // Verify the Firebase ID token and decode it
    public String getIdToken(String bearerToken) throws FirebaseAuthException {
        try {
            // Extract the token (assuming it's in the format "Bearer <JWT>")
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                throw new IllegalArgumentException("Invalid bearer token");
            }
            String idToken = bearerToken.substring(7); // Remove "Bearer " from the header

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

    public String parseJSON(ResponseEntity<String> response, String fieldName) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonResponse = mapper.readTree(response.getBody());
        String field = jsonResponse.get(fieldName).asText();
        return field;
    }

    public boolean isEmailVerified(String idToken) throws Exception {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        UserRecord userRecord = FirebaseAuth.getInstance().getUser(decodedToken.getUid());
        return userRecord.isEmailVerified();
    }

    // Retrieves the idToken on login (email & password)
    public String login(Login login)
            throws ExecutionException, InterruptedException, JsonProcessingException, Exception {
        // Basic Validation
        if (login.getEmail() == null || !login.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (login.getPassword() == null || login.getPassword().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }

        // Creates a JSON template to send to Firebase Auth Client
        String requestPayload = String.format("{\"email\":\"%s\",\"password\":\"%s\",\"returnSecureToken\":true}",
                login.getEmail(), login.getPassword());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create the request entity
        HttpEntity<String> entity = new HttpEntity<>(requestPayload, headers);
        RestTemplate restTemplate = new RestTemplate();

        // @TODO: disallow login if email is not verified

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + FIREBASE_API_KEY,
                    HttpMethod.POST,
                    entity,
                    String.class);

            // Parse the response
            if (response.getStatusCode() == HttpStatus.OK) {
                String token = parseJSON(response, "idToken");
                if (!isEmailVerified(token)){
                    System.out.println("Email not verified");
                    throw new IllegalArgumentException("Please verify your account via your email to continue.");
                }
                return token;
            }
            throw new Exception("Something went wrong");
        } catch (HttpClientErrorException e) {
            // Handle HTTP client errors (4xx)
            if (e.getMessage().contains("too-many-requests")) {
                throw new TooManyRequestsException(
                        "Too many requests detected. Please try again later.");
            }
            if (e.getStatusCode() == HttpStatus.FORBIDDEN && e.getMessage().contains("blocked")) {
                throw new AccessDeniedException(e.getMessage());
            }
            // throw new IllegalArgumentException(e.getMessage());
            throw new IllegalArgumentException("Email or password is incorrect.");
        } catch (HttpServerErrorException e) {
            // Handle HTTP server errors (5xx)
            throw new HttpServerErrorException(e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public void logoutUser(String uid) throws FirebaseAuthException {
        FirebaseAuth.getInstance().revokeRefreshTokens(uid);
    }

    public Boolean userExists(String uid) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore(); // connect the db
        DocumentReference documentReference = dbFirestore.collection("user").document(uid);
        ApiFuture<DocumentSnapshot> future = documentReference.get();
        DocumentSnapshot document = future.get();
        return document.exists();
    }

    // For this Firebase doc, the uid is the documentId.
    public User getUser(String uid) throws ExecutionException, InterruptedException, Exception {
        Firestore dbFirestore = FirestoreClient.getFirestore(); // connect the db
        DocumentReference documentReference = dbFirestore.collection("user").document(uid); // get the doc
        ApiFuture<DocumentSnapshot> future = documentReference.get();
        DocumentSnapshot document = future.get();
        User user;
        if (document.exists()) {
            user = document.toObject(User.class); // convert to object
            return user;
        }
        throw new IllegalArgumentException("User not found.");
    }

    public String getUserEmail(String uid) throws ExecutionException, InterruptedException, FirebaseAuthException {
        UserRecord userRecord = FirebaseAuth.getInstance().getUser(uid);
        return userRecord.getEmail();
    }

    // this is exact same as POST route!! should we input validate?
    // @TODO: Disable setting of userStatus
    // exisits in the database!
    public String updateUser(User user, String uid) throws ExecutionException, InterruptedException {
        if (userExists(uid)) {
            Firestore dbFirestore = FirestoreClient.getFirestore(); // connect the db
            ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection("user").document(uid).set(user);
            return collectionsApiFuture.get().getUpdateTime().toString();
        }
        throw new IllegalArgumentException("User not found.");
    }

    public String updatePassword(String newPassword, String uid)
            throws ExecutionException, InterruptedException, FirebaseAuthException {
        if (!isPasswordValid(newPassword))
            throw new IllegalArgumentException(
                    "Password should be between 8-32 characters, at least 1 uppercase, 1 lowercase letter, 1 digit and 1 special character.");
        UpdateRequest request = new UserRecord.UpdateRequest(uid).setPassword(newPassword);
        FirebaseAuth.getInstance().updateUser(request);
        return "Successfully updated password";
    }

    // For this Firebase doc, the uid is the documentId.
    public String deleteUser(String uid) throws ExecutionException, InterruptedException, FirebaseAuthException {
        FirebaseAuth.getInstance().deleteUser(uid);
        Firestore dbFirestore = FirestoreClient.getFirestore(); // connect the db
        ApiFuture<WriteResult> writeResult = dbFirestore.collection("user").document(uid).delete();
        return "Successfully deleted " + uid;
    }

    // Birthday date format checks DD/MM/YYYY
    public static boolean isBirthdayValid(String birthday) {
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

    // Checks if username exists.
    public static boolean isUsernameUnique(String username) throws ExecutionException, InterruptedException {
        String normalizedUsername = username.toLowerCase();
        Firestore dbFirestore = FirestoreClient.getFirestore();

        ApiFuture<QuerySnapshot> future = dbFirestore.collection("user")
                .whereEqualTo("userName", normalizedUsername)
                .get();

        QuerySnapshot querySnapshot = future.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

        return documents.isEmpty();
    }
}
