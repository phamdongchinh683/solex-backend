package com.example.solex_backend.dto.response;

public record ShippingFeeResponse(
        Double estimatedKm,
        Double fee) {
}
