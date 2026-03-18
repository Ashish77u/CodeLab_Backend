package com.codelab.backend.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public record ProjectResponse(
//        Long id,
//        String title,
//        String description,
//        String about,
//        String coverImageUrl,
//        String zipFileName,
//        Long zipFileSize,
//        String tags,
//        String downloadCount,
//        String published,
//        Set<String> createdAt,
//        long updatedAt,
//        String uploaderUsername,
//        String uploaderRealName,
//        String uploaderProfileImageUrl,

        // Uploader fields — ADD THESE
//        boolean uploaderUsername,
//        LocalDateTime uploaderRealName,
//        LocalDateTime uploaderProfileImageUrl



                Long id,
        String title,
        String description,
        String about,
        String coverImageUrl,
        String zipFileName,
        String zipFileUrl,        // ← ADD THIS
        long downloadCount,
        LocalDateTime createdAt,
        List<String> tags,

        // Uploader fields — ADD THESE
        String uploaderUsername,
        String uploaderRealName,
        String uploaderBio,
        String uploaderLocation,
        String uploaderEducation,
        String uploaderWork,
        String uploaderProfileImageUrl,
        LocalDateTime uploaderJoinedDate



) {}