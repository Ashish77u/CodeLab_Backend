package com.codelab.backend.exception;

// ── ErrorResponse.java ─────────────────────────────────────
import java.time.LocalDateTime;

public record ErrorResponse(
        int status,
        String error,
        String message,
        String path,
        LocalDateTime timestamp
) {}