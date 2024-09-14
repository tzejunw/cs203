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

    @GetMapping("/user/get") // documentId is the user's email. The argument here determines what it expects as the key in Postman
    public User getUser(@RequestParam String documentId) throws InterruptedException, ExecutionException {
        return userService.getUser(documentId);
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
