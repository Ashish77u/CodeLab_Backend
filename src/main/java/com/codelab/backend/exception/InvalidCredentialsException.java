package com.codelab.backend.exception;


import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends AppException {
    public InvalidCredentialsException(String msg) {
        super(msg, HttpStatus.UNAUTHORIZED);   // 401
    }
}
