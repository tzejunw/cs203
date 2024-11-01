package com.java.firebase.demo.user;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.cloud.firestore.FirestoreException;
import com.google.firebase.auth.FirebaseAuthException;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping("/user") // expects a User object in body raw JSON
    public ResponseEntity<String> createUser(@RequestBody UserCredentials userCredentials) throws InterruptedException, ExecutionException, FirebaseAuthException, FirestoreException {
        String uid = userService.createUser(userCredentials);
        return ResponseEntity.ok().body(uid);
    }

    // User profile creation is separated from account creation as
    // 1. firebase authentication does not allow storing of custom fields
    // 2. Need to account for Google Sign-in flow
    // 3. To speed up the function as previously it was taking 8 seconds to load.
    @PostMapping("/user/profile") // expects a User object in body raw JSON
    public ResponseEntity<String> createUserDetails(@RequestBody User user, HttpServletRequest request) throws InterruptedException, ExecutionException, FirebaseAuthException, FirestoreException {
        String uid = userService.getIdToken(request.getHeader("Authorization"));
        userService.createUserDetails(user, uid);
        return ResponseEntity.ok().body("Success");
    }
    
    @PostMapping("/user/resendVerification") 
    public ResponseEntity<String> resendVerificationLink(@RequestParam String email) throws InterruptedException, ExecutionException, JsonProcessingException, Exception {
        userService.sendVerificationEmail(email);
        return ResponseEntity.ok().body("Success");
    }

    @PostMapping("/user/verifyEmail") 
    public ResponseEntity<String> verifyEmail(@RequestBody VerifyEmail verifyEmail) throws InterruptedException, ExecutionException, JsonProcessingException, Exception {
        userService.verifyUserEmail(verifyEmail.getUid());
        return ResponseEntity.ok().body("Success");
    }

    @PostMapping("/login") // expects a User object in body raw JSON
    public ResponseEntity<String> login(@RequestBody UserCredentials userCredentials) throws InterruptedException, ExecutionException, JsonProcessingException, Exception {
        String bearerToken = userService.login(userCredentials);
        return ResponseEntity.ok().body(bearerToken);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) throws InterruptedException, ExecutionException, FirebaseAuthException, FirestoreException, Exception {
        String uid = userService.getIdToken(request.getHeader("Authorization"));
        userService.logoutUser(uid);
        return ResponseEntity.ok("User signed out successfully.");
    }

    // Uses the token ID from login > convert it to uid > get user data from firestore
    // To test in postman: Authorization > Under Auth Type: Bearer Token > Token (put the respective token ID from login)
    @GetMapping("/user") 
    public ResponseEntity<?> getUser(HttpServletRequest request) throws InterruptedException, ExecutionException, FirebaseAuthException, FirestoreException, Exception {
        String uid = userService.getIdToken(request.getHeader("Authorization"));
        User user = userService.getUser(uid);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/allPlayers") // doesnt expect anything
    public ResponseEntity<?> getAllUsers() throws InterruptedException, ExecutionException {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/player") // Expects String in Param
    public ResponseEntity<?> getPlayer(@RequestParam String userName) throws InterruptedException, ExecutionException {
        User user = userService.getPlayer(userName);
        return ResponseEntity.ok(user);
    }


    // Email is seperated as further email verification (when updated) is necessary later
    // @GetMapping("/user/getEmail") 
    // public ResponseEntity<String> getUserEmail(HttpServletRequest request) throws InterruptedException, ExecutionException, FirebaseAuthException {
    //     String uid = userService.getIdToken(request.getHeader("Authorization"));
    //     return ResponseEntity.ok().body(userService.getUserEmail(uid));
    // }

    @PutMapping("/user") // expects a User object in body raw JSON
    public ResponseEntity<String> updateUser(@RequestBody User user, HttpServletRequest request) throws InterruptedException, ExecutionException, FirebaseAuthException {
        String uid = userService.getIdToken(request.getHeader("Authorization"));
        userService.updateUser(user, uid);
        return ResponseEntity.ok().body("Profile successfully updated!");
    }

    @PutMapping("/user/password")
    public ResponseEntity<String> updatePassword(@RequestBody UpdatePassword updatePassword, HttpServletRequest request) throws InterruptedException, ExecutionException, FirebaseAuthException {
        String uid = userService.getIdToken(request.getHeader("Authorization"));
        userService.updatePassword(updatePassword.getPassword(), uid);
        return ResponseEntity.ok().body("Password successfully updated!");
    }

    @DeleteMapping("/user") // documentId is the user's email. The argument here determines what it expects as the key in Postman
    public ResponseEntity<String> deleteUser(HttpServletRequest request) throws InterruptedException, ExecutionException, FirebaseAuthException {
        String uid = userService.getIdToken(request.getHeader("Authorization"));
        userService.deleteUser(uid);
        return ResponseEntity.ok().body("Successfully deleted user!");
    }
}
