package com.java.firebase.demo.user;

import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreException;
import com.google.firebase.auth.FirebaseAuthException;

public class UserServiceTest {

    // @Mock
    // private Firestore dbFirestore;

    // @InjectMocks
    // private UserService userService;

    // // @BeforeAll
    // // public static void setup() {
    // //     dbFirestore = mock(Firestore.class);
    // //     userService = new UserService(dbFirestore);
    // // }

    // @Test
    // public void testCreateUserDetails_ValidUser() throws ExecutionException, InterruptedException, FirestoreException {
    //     User user = new User("ValidUsername", "Sandy", "Male", "12/12/2000");
    //     when(userService.isUsernameUnique(user.getUserName())).thenReturn(true);
    //     // when(userService.isBirthdayValid(user.getBirthday())).thenReturn(true);
    //     // when(userService.isUsernameValid(user.getUserName())).thenReturn(true);

        
    //     try {
    //         userService.createUserDetails(user, "validUid");
    //     } catch (FirestoreException | FirebaseAuthException | ExecutionException | InterruptedException e) {
    //         e.printStackTrace();
    //     }

    //     verify(dbFirestore).collection("user").document("validUid").set(user);
    // }
}