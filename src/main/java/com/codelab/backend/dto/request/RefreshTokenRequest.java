package com.codelab.backend.dto.request;


// ── dto/request/RefreshTokenRequest.java ───────────────────

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank String refreshToken
) {}


