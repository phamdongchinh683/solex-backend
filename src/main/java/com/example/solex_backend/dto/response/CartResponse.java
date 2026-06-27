package com.example.solex_backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record CartResponse(
                @Schema(description = "Restaurant ID of items in cart, null if cart is empty") Long restaurantId,
                @Schema(description = "Restaurant latitude") Double restaurantLatitude,
                @Schema(description = "Restaurant longitude") Double restaurantLongitude,
                @Schema(description = "Cart items") List<CartItemResponse> items) {
}
