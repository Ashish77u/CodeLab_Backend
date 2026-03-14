package com.codelab.backend.dto.response;

// ── dto/response/AuthResponse.java ─────────────────────────

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
//        String email
        UserSummaryResponse user
) {}