package com.codelab.backend.dto.response;


import java.time.LocalDateTime;
import java.util.Set;

public record ProjectCardResponse(
        Long id, String title, String description, String coverImageUrl,
        String uploaderUsername, String uploaderProfileImageUrl,
        Set<String> tags, long downloadCount, LocalDateTime createdAt
) {}