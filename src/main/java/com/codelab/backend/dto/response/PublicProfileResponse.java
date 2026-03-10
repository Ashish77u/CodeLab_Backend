package com.codelab.backend.dto.response;

import java.time.LocalDateTime;

// What ANYONE can see when viewing a user's profile
// NOTE: No email, no role — those are private
public record PublicProfileResponse(
        Long id,
        String username,
        String realName,
        String bio,
        String location,
        String education,
        String work,
        String websiteUrl,
        String profileImageUrl,
        LocalDateTime joinedDate,
        long totalProjects,
        long totalDownloads
) {}
