package com.codelab.backend.exception;


import org.springframework.http.HttpStatus;

public class UserNotFoundException extends AppException {
    public UserNotFoundException(String msg) {
        super(msg, HttpStatus.NOT_FOUND);   // 404
    }
}


