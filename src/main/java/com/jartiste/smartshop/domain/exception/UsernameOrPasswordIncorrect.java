package com.jartiste.smartshop.domain.exception;

public class UsernameOrPasswordIncorrect extends RuntimeException {
    public UsernameOrPasswordIncorrect(String message) {
        super(message);
    }
}
