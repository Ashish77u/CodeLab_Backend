package com.codelab.backend.dto.request;

// ── dto/request/LoginRequest.java ──────────────────────────

import jakarta.validation.constraints.*;

public record LoginRequest(

        // Named "email" but accepts both email and username
        // No @Email annotation — that would reject plain usernames
        @NotBlank(message = "Email or username is required")
        String email,

        @NotBlank(message = "Password is required")
        String password
) {}


