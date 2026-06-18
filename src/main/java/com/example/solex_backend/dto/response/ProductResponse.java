package com.example.solex_backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

public record ProductResponse(
        @Schema(description = "Product ID") Long id,
        @Schema(description = "Product name") String name,
        @Schema(description = "Product description") String description,
        @Schema(description = "Base price") BigDecimal basePrice,
        @Schema(description = "Is active") Boolean isActive,
        @Schema(description = "Category ID") Long categoryId,
        @Schema(description = "Category name") String categoryName,
        @Schema(description = "Product images") List<String> images,
        @Schema(description = "Product variants") List<ProductVariantResponse> variants
) {
}
