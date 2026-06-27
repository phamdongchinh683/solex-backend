package com.example.solex_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record PresignRequest(
        @NotBlank
        @Schema(description = "Target Cloudinary folder, e.g. 'avatars' or 'products'")
        String folder
) {}
