package com.java.firebase.demo;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


// some boiler plate to authenticate and connect to firebase

@SpringBootApplication(scanBasePackages = {"com.java.firebase.demo","com.java.firebase.demo.user","com.java.firebase.demo.tournament"})
public class CRUDRunner {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(CRUDRunner.class, args);
    }
}