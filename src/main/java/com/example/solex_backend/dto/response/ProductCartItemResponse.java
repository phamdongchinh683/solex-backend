package com.example.solex_backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProductCartItemResponse(
        @Schema(description = "Product ID") Long id,
        @Schema(description = "Product name") String name,
        @Schema(description = "Product description") String description,
        @Schema(description = "Product image") String image,
        @Schema(description = "Product variants") ProductVariantResponse variant) {
}
