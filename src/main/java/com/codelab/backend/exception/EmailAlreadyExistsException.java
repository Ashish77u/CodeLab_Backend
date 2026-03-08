package com.codelab.backend.exception;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// ── Specific Exceptions ────────────────────────────────────
 import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends AppException {
    public EmailAlreadyExistsException(String msg) {
        super(msg, HttpStatus.CONFLICT);   // 409
    }
}
