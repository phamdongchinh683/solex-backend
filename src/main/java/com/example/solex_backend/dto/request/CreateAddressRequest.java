package com.example.solex_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateAddressRequest(
        @Schema(description = "Recipient first name")
        @NotBlank(message = "firstName is required")
        String firstName,

        @Schema(description = "Recipient last name")
        @NotBlank(message = "lastName is required")
        String lastName,

        @Schema(description = "Recipient phone number (10-14 digits)")
        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\d{10,14}$", message = "Phone number must be 10-14 digits")
        String phoneNumber,

        @Schema(description = "Full address detail")
        @NotBlank(message = "Address detail is required")
        String addressDetail,

        @Schema(description = "Longitude (-180 to 180)")
        @NotNull(message = "Longitude is required")
        @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
        @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
        Double longitude,

        @Schema(description = "Latitude (-90 to 90)")
        @NotNull(message = "Latitude is required")
        @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
        @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
        Double latitude
) {}
