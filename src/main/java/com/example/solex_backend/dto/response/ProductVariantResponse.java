package com.example.solex_backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record ProductVariantResponse(
        @Schema(description = "Variant ID") Long id,
        @Schema(description = "SKU") String sku,
        @Schema(description = "Price") BigDecimal price,
        @Schema(description = "Image URL") String image,
        @Schema(description = "Size") String size,
        @Schema(description = "Name") String name,
        @Schema(description = "Is active") Boolean isActive
) {
}