package com.codelab.backend.dto.request;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record UpdateProfileRequest(

        // All fields are optional (null = don't update that field)
        // Frontend only sends fields the user actually changed

        @Size(max = 50, message = "Real name max 50 characters")
        String realName,

        @Size(max = 200, message = "Bio max 200 characters")
        String bio,

        @Size(max = 100, message = "Location max 100 characters")
        String location,

        @Size(max = 100, message = "Education max 100 characters")
        String education,

        @Size(max = 100, message = "Work max 100 characters")
        String work,

        @Pattern(
                regexp = "^(https?://.*)?$",
                message = "Website URL must start with http:// or https://"
        )
        String websiteUrl

) {}