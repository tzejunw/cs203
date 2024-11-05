package com.java.firebase.demo.Exceptions;

public class TooManyRequestsException extends Exception {
    public TooManyRequestsException(String message) {
        super(message);
    }
}
