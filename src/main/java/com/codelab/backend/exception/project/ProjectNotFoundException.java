package com.codelab.backend.exception.project;

import com.codelab.backend.exception.AppException;
import org.springframework.http.HttpStatus;

public class ProjectNotFoundException extends AppException {
    public ProjectNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}