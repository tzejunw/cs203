package com.java.firebase.demo.user;

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
            // Get the JWT from the Authorization header
            String authorizationHeader = request.getHeader("Authorization");

            // Extract the token (assuming it's in the format "Bearer <JWT>")
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return new User();
            }
            String idToken = authorizationHeader.substring(7);  // Remove "Bearer " from the header

            String uid = userService.getIdToken(idToken);
            return userService.getUser(uid);
        } catch (Exception e) {
            // @TODO: Some proper error handling lmao
            return new User();
        }
        
    }

    @PutMapping("/user/update") // expects a User object in body raw JSON
    public String updateUser(@RequestBody User user) throws InterruptedException, ExecutionException {
        return userService.updateUser(user);
    }

    @DeleteMapping("/user/delete") // documentId is the user's email. The argument here determines what it expects as the key in Postman
    public String deleteUser(@RequestParam String documentId) throws InterruptedException, ExecutionException {
        return userService.deleteUser(documentId);
    }

    @GetMapping("/user/test")
    public ResponseEntity<String> testGetEndpoint(){
        return ResponseEntity.ok("Test Get Endpoint is Working");
    }
    
    
}
