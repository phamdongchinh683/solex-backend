package com.example.solex_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record CreateProductRequest(
        @Schema(description = "Product name")
        @NotBlank(message = "Product name cannot be empty")
        String name,

        @Schema(description = "Product description")
        String description,

        @Schema(description = "Restaurant ID")
        @NotNull(message = "Restaurant ID cannot be null")
        Long restaurantId,

        @Schema(description = "Category ID")
        @NotNull(message = "Category ID cannot be null")
        Long categoryId,

        @Schema(description = "Base price")
        @NotNull(message = "Base price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Base price must be greater than 0")
        BigDecimal basePrice,

        @Schema(description = "Is product active")
        Boolean isActive,

        @Schema(description = "Product images URLs")
        List<String> images
) {}
