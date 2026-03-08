package com.codelab.backend.exception;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;


public class UsernameAlreadyExistsException extends AppException {
    public UsernameAlreadyExistsException(String msg) {
        super(msg, HttpStatus.CONFLICT);   // 409
    }
}
