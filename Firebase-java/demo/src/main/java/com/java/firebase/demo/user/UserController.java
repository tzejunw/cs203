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
    public String createUser(@RequestBody Register register) throws InterruptedException, ExecutionException {
        return userService.createUser(register);
    }

    @PostMapping("/user/login") // expects a User object in body raw JSON
    public String login(@RequestBody Login login) throws InterruptedException, ExecutionException, JsonProcessingException {
        return userService.login(login);
    }

    // Uses the token ID from login > convert it to uid > get user data from firestore
    // To test in postman: Authorization > Under Auth Type: Bearer Token > Token (put the respective token ID from login)
    @GetMapping("/user/get") 
    public User getUser(HttpServletRequest request) throws InterruptedException, ExecutionException {
        try {
            String uid = userService.getIdToken(request.getHeader("Authorization"));
            return userService.getUser(uid);
        } catch (Exception e) {
            // @TODO: Some proper error handling lmao
            return new User();
        }
    }

    // Email is seperated as further email verification (when updated) is necessary later
    @GetMapping("/user/getemail") 
    public String getUserEmail(HttpServletRequest request) throws InterruptedException, ExecutionException, FirebaseAuthException {
        try {
            String uid = userService.getIdToken(request.getHeader("Authorization"));
            return userService.getUserEmail(uid);
        } catch (Exception e) {
            // @TODO: Some proper error handling lmao
            return "Error: " + e.getMessage();
        }
    }

    @PutMapping("/user/update") // expects a User object in body raw JSON
    public String updateUser(@RequestBody User user, HttpServletRequest request) throws InterruptedException, ExecutionException, FirebaseAuthException {
        String uid = userService.getIdToken(request.getHeader("Authorization"));
        return userService.updateUser(user, uid);
    }

    @DeleteMapping("/user/delete") // documentId is the user's email. The argument here determines what it expects as the key in Postman
    public String deleteUser(HttpServletRequest request) throws InterruptedException, ExecutionException {
        try {
            String uid = userService.getIdToken(request.getHeader("Authorization"));
            return userService.deleteUser(uid);
        } catch (Exception e) {
            // @TODO: Some proper error handling lmao
            return e.getMessage();
        }
    }

    @GetMapping("/user/test")
    public ResponseEntity<String> testGetEndpoint(){
        return ResponseEntity.ok("Test Get Endpoint is Working");
    }
    
    
}
