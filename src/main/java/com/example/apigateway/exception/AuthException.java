package com.example.apigateway.exception;

/**
 * AuthException class
 */
public class AuthException extends RuntimeException {

    public AuthException(String message) {
        super(message);
    }
}
