package com.java.firebase.demo.user;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockitoAnnotations;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreException;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    // @Mock
    // private Firestore dbFirestore;

    // @Mock
    // private FirebaseAuth firebaseAuth;

    // @Mock
    // private CollectionReference collectionReference;

    // @Mock
    // private Query query;

    // @Mock
    // private ApiFuture<QuerySnapshot> future;

    // @Mock
    // private QuerySnapshot querySnapshot;

    // @InjectMocks
    // private UserService userService;

    // @Test
    // public void testCreateUserDetails_ValidUser() throws ExecutionException, InterruptedException, FirestoreException {        
    //     // Arrange
    //     String testUsername = "UniqueUsername";
    //     String normalizedUsername = testUsername.toLowerCase();

    //     // Mocking the Firestore behavior
    //     when(dbFirestore.collection("user")).thenReturn(collectionReference);
    //     when(collectionReference.whereEqualTo("userName", normalizedUsername)).thenReturn(query);
    //     when(query.get()).thenReturn(future);
    //     when(future.get()).thenReturn(querySnapshot);
    //     when(querySnapshot.getDocuments()).thenReturn(Collections.emptyList());

    //     // Act
    //     boolean isUnique = userService.isUsernameUnique(testUsername);

    //     // Assert
    //     assertTrue(isUnique);
    // }
}