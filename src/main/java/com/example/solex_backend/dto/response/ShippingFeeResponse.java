package com.example.solex_backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record ShippingFeeResponse(
        @Schema(description = "Estimated route distance in kilometers")
        Double estimatedKm,

        @Schema(description = "Shipping fee in VND")
        Double fee,

        @Schema(description = "Estimated travel duration in seconds")
        Double estimatedDurationSeconds,

        @Schema(description = "Route coordinate pairs [[lng, lat], ...] for map rendering")
        List<List<Double>> routeCoordinates
) {}
