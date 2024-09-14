package com.java.firebase.demo.user;

import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.cloud.FirestoreClient;

@Service
public class UserService {
    // This takes one of the specified json fields, here .getEmail(), and sets it as
    // the documentId (document identifier) if you want firebase to generate documentId for us, leave .document() blank
    public String createUser(Register register) throws ExecutionException, InterruptedException {
        // Step 1: Validate if registration details fits our business requirements
        // Email validations is done by Firebase Auth
        if (!isUsernameValid(register.getUserName()))
            return "Error: Username should be between 3-32 characters long";
        if (!isUsernameUnique(register.getUserName()))
            return "Error: Username exists, please choose another username";
        if (!isPasswordValid(register.getPassword()))
            return "Error: Password should be between 8-32 characters, at least 1 uppercase and lowercase letter, 1 digit and 1 special character.";
        
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

    // For this Firebase doc, the email is the documentId.
    // The key in the GET request must be "documentid", and the value is the email
    public User getUser(String documentId) throws ExecutionException, InterruptedException {
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
    // TODO input validate that the json has "documentId" and that its value pair
    // exisits in the database!
    public String updateUser(User user) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore(); // connect the db
        ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection("user").document(user.getEmail())
                .set(user);
        return collectionsApiFuture.get().getUpdateTime().toString();
    }

    // For this Firebase doc, the email is the documentId.
    // The key in the GET request must be "documentid", and the value is the email
    public String deleteUser(String documentId) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore(); // connect the db
        ApiFuture<WriteResult> writeResult = dbFirestore.collection("user").document(documentId).delete();
        return "Successfully deleted " + documentId;
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
    private static final int USERNAME_MIN_LENGTH = 8;
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
