package com.codelab.backend.dto.response;

// ── dto/response/UserSummaryResponse.java ──────────────────

import java.time.LocalDateTime;

public record UserSummaryResponse(
        Long id,
        String username,
        String email,
        String profileImageUrl,
        String role,
        LocalDateTime joinedDate
) {}


