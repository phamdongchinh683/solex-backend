package com.example.solex_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OperatorSignupRequest(
        @Schema(description = "Operator email")
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @Schema(description = "Password (min 6 characters)")
        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password,

        @Schema(description = "First name")
        @NotBlank(message = "First name is required")
        String firstName,

        @Schema(description = "Last name")
        @NotBlank(message = "Last name is required")
        String lastName,

        @Schema(description = "Phone number")
        String phone,

        @Schema(description = "Restaurant address detail")
        @NotBlank(message = "Address detail is required")
        String addressDetail,

        @Schema(description = "Restaurant longitude")
        @NotNull(message = "Longitude is required")
        Double longitude,

        @Schema(description = "Restaurant latitude")
        @NotNull(message = "Latitude is required")
        Double latitude,

        @Schema(description = "Restaurant information")
        @NotNull(message = "Restaurant information is required")
        @Valid
        RestaurantInfo restaurant
) {
    public record RestaurantInfo(
            @Schema(description = "Restaurant name")
            @NotBlank(message = "Restaurant name is required")
            String name,

            @Schema(description = "Description")
            String description,

            @Schema(description = "Restaurant contact phone")
            String phone,

            @Schema(description = "Cover image URL")
            String imageUrl
    ) {}
}
