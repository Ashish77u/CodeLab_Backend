package com.codelab.backend.exception.project;

import com.codelab.backend.exception.AppException;
import org.springframework.http.HttpStatus;

public class AccessDeniedException extends AppException {
    public AccessDeniedException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}