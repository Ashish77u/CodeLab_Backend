package com.codelab.backend.dto.response;

import java.time.LocalDateTime;
import java.util.Set;

public record ProjectResponse(
        Long id, String title, String description, String about,
        String coverImageUrl, String zipFileName, Long zipFileSize,
        String uploaderUsername, String uploaderRealName,
        String uploaderProfileImageUrl, Set<String> tags,
        long downloadCount, boolean published,
        LocalDateTime createdAt, LocalDateTime updatedAt
) {}