package com.example.solex_backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record AddressResponse(
        @Schema(description = "Address ID")        Long id,
        @Schema(description = "First name")        String firstName,
        @Schema(description = "Last name")         String lastName,
        @Schema(description = "Full name")         String fullName,
        @Schema(description = "Phone")             String phone,
        @Schema(description = "Longitude")         double longitude,
        @Schema(description = "Latitude")          double latitude,
        @Schema(description = "Address detail")    String addressDetail,
        @Schema(description = "Is default")        Boolean isDefault,
        @Schema(description = "Created at")        LocalDateTime createdAt
) {}
