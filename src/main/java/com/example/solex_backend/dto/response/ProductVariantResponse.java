package com.example.solex_backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record ProductVariantResponse(
        @Schema(description = "Variant ID") Long id,
        @Schema(description = "SKU") String sku,
        @Schema(description = "Size") String size,
        @Schema(description = "Price") BigDecimal price,
        @Schema(description = "Stock") Integer stock,
        @Schema(description = "Image URL") String imageUrl,
        @Schema(description = "Is active") Boolean isActive
) {
}