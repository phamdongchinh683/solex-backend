package com.example.solex_backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record RestaurantResponse(
                @Schema(description = "Restaurant ID") Long id,
                @Schema(description = "Restaurant name") String name,
                @Schema(description = "Restaurant description") String description,
                @Schema(description = "Phone number") String phone,
                @Schema(description = "Address detail") String addressDetail,
                @Schema(description = "Longitude") double longitude,
                @Schema(description = "Latitude") double latitude,
                @Schema(description = "Number of 1-star ratings") Integer star1,
                @Schema(description = "Number of 2-star ratings") Integer star2,
                @Schema(description = "Number of 3-star ratings") Integer star3,
                @Schema(description = "Number of 4-star ratings") Integer star4,
                @Schema(description = "Number of 5-star ratings") Integer star5,
                @Schema(description = "Is accepting orders") Boolean isOpen,
                @Schema(description = "Image URL") String imageUrl) {
}
