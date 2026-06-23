package com.example.solex_backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record RatingResponse(
        @Schema(description = "Rating ID") Long id,
        @Schema(description = "Restaurant ID") Long restaurantId,
        @Schema(description = "User ID") Long userId,
        @Schema(description = "Restaurant rating from 1 to 5") Integer rating,
        @Schema(description = "Rating comment") String comment,
        @Schema(description = "Created at") LocalDateTime createdAt
) {
}
