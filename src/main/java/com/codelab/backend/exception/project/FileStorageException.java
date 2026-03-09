package com.codelab.backend.exception.project;

import com.codelab.backend.exception.AppException;
import org.springframework.http.HttpStatus;

public class FileStorageException extends AppException {
    public FileStorageException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}