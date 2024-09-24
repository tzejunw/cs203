package com.java.firebase.demo.user;

import java.util.concurrent.ExecutionException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.firebase.auth.FirebaseAuthException;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class UserController {

    public UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping("/user/create") // expects a User object in body raw JSON
    public ResponseEntity<String> createUser(@RequestBody Register register) throws InterruptedException, ExecutionException {
        try {
            userService.createUser(register);
            return ResponseEntity.ok().body("Welcome " + register.getName() + ", to Magic Arena. Login to continue.");
        } catch (IllegalArgumentException | FirebaseAuthException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/user/login") // expects a User object in body raw JSON
    public ResponseEntity<String> login(@RequestBody Login login) throws InterruptedException, ExecutionException, JsonProcessingException, Exception {
        try {
            String bearerToken = userService.login(login);
            return ResponseEntity.ok().body(bearerToken);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // Uses the token ID from login > convert it to uid > get user data from firestore
    // To test in postman: Authorization > Under Auth Type: Bearer Token > Token (put the respective token ID from login)
    @GetMapping("/user/get") 
    public ResponseEntity<?> getUser(HttpServletRequest request) throws InterruptedException, ExecutionException {
        try {
            String uid = userService.getIdToken(request.getHeader("Authorization"));
            User user = userService.getUser(uid);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred: " + e.getMessage());
        }
    }

    // Email is seperated as further email verification (when updated) is necessary later
    @GetMapping("/user/getEmail") 
    public ResponseEntity<String> getUserEmail(HttpServletRequest request) throws InterruptedException, ExecutionException, FirebaseAuthException {
        try {
            String uid = userService.getIdToken(request.getHeader("Authorization"));
            return ResponseEntity.ok().body(userService.getUserEmail(uid));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred: " + e.getMessage());
        }
    }

    @PutMapping("/user/update") // expects a User object in body raw JSON
    public ResponseEntity<String> updateUser(@RequestBody User user, HttpServletRequest request) throws InterruptedException, ExecutionException, FirebaseAuthException {
        try {
            String uid = userService.getIdToken(request.getHeader("Authorization"));
            userService.updateUser(user, uid);
            return ResponseEntity.ok().body("Profile successfully updated!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred: " + e.getMessage());
        }
    }

    // Segregated for backend simplicity
    @PutMapping("/user/updateEmail")
    public ResponseEntity<String> updateEmail(@RequestBody UpdateEmail updateEmail, HttpServletRequest request) throws InterruptedException, ExecutionException, FirebaseAuthException {
        try {
            String uid = userService.getIdToken(request.getHeader("Authorization"));
            userService.updateEmail(updateEmail.getEmail(), uid);
            return ResponseEntity.ok().body("Email successfully updated!");
        } catch (IllegalArgumentException | FirebaseAuthException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred: " + e.getMessage());
        }
    }

    // Segregated for frontend simplicity
    @PutMapping("/user/updatePassword")
    public ResponseEntity<String> updatePassword(@RequestBody UpdatePassword updatePassword, HttpServletRequest request) throws InterruptedException, ExecutionException, FirebaseAuthException {
        try {
            String uid = userService.getIdToken(request.getHeader("Authorization"));
            userService.updatePassword(updatePassword.getPassword(), uid);
            return ResponseEntity.ok().body("Password successfully updated!");
        } catch (IllegalArgumentException | FirebaseAuthException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred: " + e.getMessage());
        }
    }

    @DeleteMapping("/user/delete") // documentId is the user's email. The argument here determines what it expects as the key in Postman
    public ResponseEntity<String> deleteUser(HttpServletRequest request) throws InterruptedException, ExecutionException {
        try {
            String uid = userService.getIdToken(request.getHeader("Authorization"));
            userService.deleteUser(uid);
            return ResponseEntity.ok().body("Successfully deleted user!");
        } catch (IllegalArgumentException | FirebaseAuthException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred: " + e.getMessage());
        }
        
    }

    @GetMapping("/user/test")
    public ResponseEntity<String> testGetEndpoint(){
        return ResponseEntity.ok("Test Get Endpoint is Working");
    }
    
    
}
