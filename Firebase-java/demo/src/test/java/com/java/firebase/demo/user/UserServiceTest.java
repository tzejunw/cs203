package com.java.firebase.demo.user;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreException;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.CreateRequest;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private Firestore dbFirestore;

    @Mock
    private FirebaseAuth firebaseAuth;

    @Mock
    private DocumentReference documentReference;

    @Mock
    private ApiFuture<DocumentSnapshot> apiFuture;

    @Mock
    private DocumentSnapshot documentSnapshot;

    @Mock
    private CollectionReference collectionReference;

    @Mock
    private Query query;

    @Mock
    private ApiFuture<QuerySnapshot> future;

    @Mock
    private QuerySnapshot querySnapshot;

    @Mock
    private ApiFuture<WriteResult> writeResultApiFuture;

    @Mock
    private WriteResult writeResult;

    @InjectMocks
    private UserService userService;

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void testCreateUserDetails_ValidDetails() throws ExecutionException, InterruptedException, FirestoreException {
        User user = new User("hahahaha", "Nina Kan", "12/12/1990", "Female");
        String uid = "testUid";

        // Mock Firestore interactions
        DocumentReference documentReference = mock(DocumentReference.class);
        CollectionReference collectionReference = mock(CollectionReference.class);
        when(dbFirestore.collection("user")).thenReturn(collectionReference);
        when(collectionReference.document(uid)).thenReturn(documentReference);

        // When
        try {
            userService.createUserDetails(user, uid);
            verify(userService).createUserDetails(user, uid);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    @Test
    void testCreateUserDetails_InvalidGender() {
        User user = new User("hahahaha", "Nina Kan", "12/12/1990", "NonBinary");
        String uid = "testUid";

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userService.createUserDetails(user, uid);
        }, "Gender must be 'Male' or 'Female'");
    }

    @Test
    void testCreateUserDetails_InvalidBirthday() {
        User user = new User("hahahaha", "Nina Kan", "12/13/1990", "Female");

        String uid = "testUid";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUserDetails(user, uid);
        });

        assertEquals("Incorrect birthday format, format should be DD/MM/YYYY", exception.getMessage());
    }

    @Test
    void testCreateUserDetails_InvalidUsername() {
        User user = new User("f", "Nina Kan", "12/12/1990", "Female");

        String uid = "testUid";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUserDetails(user, uid);
        });

        assertEquals("Username should be between 3-32 characters long", exception.getMessage());
    }

    @Test
    void testCreateAccountInAuth_Success() throws FirebaseAuthException, ExecutionException, InterruptedException {
        // Arrange
        String email = "test@example.com";
        String password = "1@Secured";
        String expectedUid = "testUid";

        // Mock the response of FirebaseAuth
        UserRecord mockUserRecord = mock(UserRecord.class);
        when(mockUserRecord.getUid()).thenReturn(expectedUid);

        CreateRequest createRequest = new CreateRequest().setEmail(email).setPassword(password);
        when(firebaseAuth.createUser(any(CreateRequest.class))).thenReturn(mockUserRecord);

        // Act
        String actualUid = userService.createAccountInAuth(email, password);

        // Assert
        assertEquals(expectedUid, actualUid); // Verify the returned UID matches the expected UID
        verify(firebaseAuth).createUser(any(CreateRequest.class)); // Verify createUser was called once
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void testCreateAccountInAuth_InvalidEmail() throws FirebaseAuthException {
        // Arrange
        String email = "invalidEmail.com";
        String password = "1@Secured";

        // Mock FirebaseAuth to throw an exception
        when(firebaseAuth.createUser(any(CreateRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid Email"));

        // Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.createAccountInAuth(email, password);
        });
    }

    @Test
    void testGetUser_Success() throws Exception {
        // Given
        String uid = "testUid";
        User expectedUser = new User("hahahaha", "Nina Kan", "12/12/1990", "Female");

        // Mock Firestore behavior
        when(dbFirestore.collection("user")).thenReturn(mock(CollectionReference.class));
        when(dbFirestore.collection("user").document(uid)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(apiFuture);
        when(apiFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.toObject(User.class)).thenReturn(expectedUser);  // Return the expected User object

        // When
        User actualUser = userService.getUser(uid);

        // Then
        assertEquals(expectedUser, actualUser);  // Check that the returned user matches the expected user
        verify(documentReference, times(1)).get();  // Ensure Firestore interactions occurred as expected
    }

    @Test
    void testGetUser_UserNotFound() throws Exception {
        // Given
        String uid = "testUid";

        // Mock Firestore behavior
        when(dbFirestore.collection("user")).thenReturn(mock(CollectionReference.class));
        when(dbFirestore.collection("user").document(uid)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(apiFuture);
        when(apiFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(false);  // Simulate document not found

        // When & Then
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.getUser(uid);
        });

        assertEquals("User not found.", exception.getMessage());
    }

    @Test
    public void testisUsernameUnique_UniqueUser() throws ExecutionException, InterruptedException, FirestoreException {
        // Arrange
        String testUsername = "UniqueUsername";
        String normalizedUsername = testUsername.toLowerCase();

        // Mocking the Firestore behavior
        when(dbFirestore.collection("user")).thenReturn(collectionReference);
        when(collectionReference.whereEqualTo("userName", normalizedUsername)).thenReturn(query);
        when(query.get()).thenReturn(future);
        when(future.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(Collections.emptyList());

        // Act
        boolean isUnique = userService.isUsernameUnique(testUsername);

        // Assert
        assertTrue(isUnique);
    }

    @Test
    public void testisUsernameUnique_NonUniqueUser()
            throws ExecutionException, InterruptedException, FirestoreException {
        // Arrange
        String testUsername = "NonUniqueUsername";
        String normalizedUsername = testUsername.toLowerCase();

        // Mocking the Firestore behavior for an existing user
        when(dbFirestore.collection("user")).thenReturn(collectionReference);
        when(collectionReference.whereEqualTo("userName", normalizedUsername)).thenReturn(query);
        when(query.get()).thenReturn(future);
        when(future.get()).thenReturn(querySnapshot);

        // Simulates a non-empty list, simulating an existing user already exists.
        QueryDocumentSnapshot mockQueryDocumentSnapshot = mock(QueryDocumentSnapshot.class);
        when(querySnapshot.getDocuments()).thenReturn(Collections.singletonList(mockQueryDocumentSnapshot));

        // Act
        boolean isUnique = userService.isUsernameUnique(testUsername);

        // Assert
        assertFalse(isUnique); // We expect the method to return false since the username is not unique
    }

    @Test
    void testUpdateUser_Success() throws Exception {
        // Given
        String uid = "testUid";
        User user = new User("hahahaha", "Nina Kan", "12/12/1990", "Female");

        // Mock Firestore behavior for userExists
        when(dbFirestore.collection("user")).thenReturn(mock(CollectionReference.class));
        when(dbFirestore.collection("user").document(uid)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(apiFuture);
        when(apiFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(true);

        // Mock Firestore behavior for updating the user
        when(dbFirestore.collection("user").document(uid).set(user)).thenReturn(writeResultApiFuture);
        when(writeResultApiFuture.get()).thenReturn(writeResult);
        when(writeResult.getUpdateTime()).thenReturn(Timestamp.now());

        // When
        String result = userService.updateUser(user, uid);

        // Then
        assertNotNull(result);
        verify(documentReference, times(1)).get();
        verify(dbFirestore.collection("user").document(uid), times(1)).set(user);
    }

    @Test
    void testUpdateUser_InvalidGender() {
        // Given
        String uid = "testUid";
        User user = new User("hahahaha", "Nina Kan", "12/12/1990", "Shemale");

        // When & Then
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUser(user, uid);
        });

        assertEquals("Gender must be 'Male' or 'Female'", exception.getMessage());
        verifyNoInteractions(dbFirestore);  // Firestore shouldn't be interacted with in this case
    }

    @Test
    void testUpdateUser_InvalidBirthday() {
        // Given
        String uid = "testUid";
        User user = new User("hahahaha", "Nina Kan", "12/13/1990", "Female");

        // When & Then
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUser(user, uid);
        });

        assertEquals("Incorrect birthday format, format should be DD/MM/YYYY", exception.getMessage());
        verifyNoInteractions(dbFirestore);  // Firestore shouldn't be interacted with in this case
    }

    @Test
    public void testDeleteUser_Success() throws ExecutionException, InterruptedException, FirebaseAuthException {
        String uid = "testUid";

        // Mock Firestore collection behavior
        CollectionReference mockCollection = mock(CollectionReference.class);
        DocumentReference mockDocument = mock(DocumentReference.class);
        ApiFuture<WriteResult> writeResult = mock(ApiFuture.class);

        when(dbFirestore.collection("user")).thenReturn(mockCollection);
        when(mockCollection.document(uid)).thenReturn(mockDocument);
        when(mockDocument.delete()).thenReturn(writeResult);

        // Call the method to test
        String result = userService.deleteUser(uid);

        // Verify interactions and assert result
        verify(firebaseAuth).deleteUser(uid);
        verify(mockDocument).delete();
        assertEquals("Successfully deleted " + uid, result);
    }

    @Test
    public void testPasswordTooShort() {
        String password = "Ab1!";  // Less than 8 characters
        assertFalse(userService.isPasswordValid(password), "Password should be invalid due to being too short");
    }

    @Test
    public void testPasswordTooLong() {
        String password = "A".repeat(30) + "b1!";  // More than 32 characters
        assertFalse(userService.isPasswordValid(password), "Password should be invalid due to being too long");
    }

    @Test
    public void testPasswordNoUpperCase() {
        String password = "abcdefg1!";  // No upper-case letter
        assertFalse(userService.isPasswordValid(password), "Password should be invalid due to missing upper case letter");
    }

    @Test
    public void testPasswordNoLowerCase() {
        String password = "ABCDEFG1!";  // No lower-case letter
        assertFalse(userService.isPasswordValid(password), "Password should be invalid due to missing lower case letter");
    }

    @Test
    public void testPasswordNoDigit() {
        String password = "Abcdefg!";  // No digit
        assertFalse(userService.isPasswordValid(password), "Password should be invalid due to missing digit");
    }

    @Test
    public void testPasswordNoSpecialChar() {
        String password = "Abcdefg1";  // No special character
        assertFalse(userService.isPasswordValid(password), "Password should be invalid due to missing special character");
    }

    @Test
    public void testPasswordValidWith8Char() {
        String password = "Abcdef1!";  // Valid password
        assertTrue(userService.isPasswordValid(password), "Password should be valid");
    }

    @Test
    public void testPasswordValidWith32Char() {
        String password = "A".repeat(29) + "b1!";  // Valid password, exactly 32 characters
        assertTrue(userService.isPasswordValid(password), "Password should be valid with 32 characters");
    }
}