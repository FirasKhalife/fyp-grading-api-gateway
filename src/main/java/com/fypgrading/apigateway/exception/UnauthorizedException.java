package com.fypgrading.apigateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * UnauthorizedException class
 */
@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends RuntimeException {

    protected String errorCode;

    public UnauthorizedException(String message) {
        super(message);
        errorCode = "UNAUTHORIZED";
    }
}
