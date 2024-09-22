package com.java.firebase.demo.user.Exceptions;

public class TooManyRequestsException extends Exception {
    public TooManyRequestsException(String message) {
        super(message);
    }
}
