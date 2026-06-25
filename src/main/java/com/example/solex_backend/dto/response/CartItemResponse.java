package com.example.solex_backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record CartItemResponse(
        @Schema(description = "Cart item ID") Long id,
        @Schema(description = "Quantity") Integer quantity,
        @Schema(description = "Product info") ProductionCartItemResponse product) {
}
