package com.vaul.vaul.exceptions.userrelated;

public class UserAlreadyPresentException extends RuntimeException {
    public UserAlreadyPresentException(String email) {
        super("User already present with email: " + email);
    }
}
