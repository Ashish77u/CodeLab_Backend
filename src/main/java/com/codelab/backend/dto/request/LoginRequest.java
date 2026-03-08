package com.codelab.backend.dto.request;

// ── dto/request/LoginRequest.java ──────────────────────────

import jakarta.validation.constraints.*;

public record LoginRequest(

        @NotBlank(message = "Email is required")
        @Email
        String email,

        @NotBlank(message = "Password is required")
        String password

) {}


