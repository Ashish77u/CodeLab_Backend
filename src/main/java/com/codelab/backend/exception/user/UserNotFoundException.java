package com.codelab.backend.exception.user;

import com.codelab.backend.exception.AppException;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends AppException {
    public UserNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);   // 404
    }
}