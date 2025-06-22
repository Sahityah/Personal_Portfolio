package com.Personal_Portfolio.Personal_Portfolio.Exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
