package com.codelab.backend.dto.response;

// ── dto/response/AuthResponse.java ─────────────────────────

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        UserSummaryResponse user
) {
    // Convenience constructor — tokenType always "Bearer"
    public AuthResponse(String accessToken, String refreshToken,
                        UserSummaryResponse user) {
        this(accessToken, refreshToken, "Bearer", user);
    }
}