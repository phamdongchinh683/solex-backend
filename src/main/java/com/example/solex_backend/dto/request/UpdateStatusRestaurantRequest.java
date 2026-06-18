package com.example.solex_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRestaurantRequest(
        @Schema(description = "Restaurant ID")
        @NotNull(message = "Restaurant ID is required")
        Long restaurantId,

        @Schema(description = "New open/closed status")
        boolean status
) {
}
