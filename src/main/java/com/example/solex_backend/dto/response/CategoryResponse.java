package com.example.solex_backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record CategoryResponse(
        @Schema(description = "Category ID") Long id,
        @Schema(description = "Category name") String name,
        @Schema(description = "Category image URL") String imageUrl
) {
}