package com.codelab.backend.dto.request;

import jakarta.validation.constraints.*;
import java.util.List;

public record ProjectUploadRequest(
        @NotBlank(message = "Title is required")
        @Size(min = 3, max = 100)
        String title,

        @NotBlank(message = "Description is required")
        @Size(min = 20)
        String description,

        @NotBlank(message = "About is required")
        String about,

        String tags
) {}

