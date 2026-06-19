package com.example.solex_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record UpdateRestaurantRequest(
        @Schema(description = "Restaurant name")    @Size(max = 200) String name,
        @Schema(description = "Description")                         String description,
        @Schema(description = "Phone number")       @Size(max = 14)  String phone,
        @Schema(description = "Address detail")     @Size(max = 100) String addressDetail,
        @Schema(description = "Longitude")                           Double longitude,
        @Schema(description = "Latitude")                            Double latitude,
        @Schema(description = "Image URL")                           String imageUrl
) {}
