package com.vaul.vaul.exceptions.userrelated;

public class UserAlredyPresent extends RuntimeException {
    public UserAlredyPresent(String email) {
        super("User already present with email: " + email);
    }
}
