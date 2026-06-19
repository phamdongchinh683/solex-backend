package com.example.solex_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateOrderRequest(
        @Schema(description = "Delivery address ID")
        @NotNull(message = "Address ID cannot be null")
        Long addressId,

        @Schema(description = "Optional coupon ID to apply a discount")
        Long couponId,

        @Schema(description = "Optional note for the order")
        @Size(max = 500, message = "Note must not exceed 500 characters")
        String note
) {
}