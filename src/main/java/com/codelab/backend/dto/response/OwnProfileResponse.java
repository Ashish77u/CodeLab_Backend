package com.codelab.backend.dto.response;

import java.time.LocalDateTime;

// What the LOGGED-IN user sees about their OWN profile
// Extends public info + adds private fields: email, role
public record OwnProfileResponse(
        Long id,
        String username,
        String email,           // ← private, not in PublicProfileResponse
        String realName,
        String bio,
        String location,
        String education,
        String work,
        String websiteUrl,
        String profileImageUrl,
        String role,            // ← private, not in PublicProfileResponse
        LocalDateTime joinedDate,
        long totalProjects,
        long totalDownloads
) {}