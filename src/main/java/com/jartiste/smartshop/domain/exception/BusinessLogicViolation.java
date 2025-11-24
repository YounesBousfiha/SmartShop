package com.jartiste.smartshop.domain.exception;

public class BusinessLogicViolation extends RuntimeException {
    public BusinessLogicViolation(String message) {
        super(message);
    }
}
