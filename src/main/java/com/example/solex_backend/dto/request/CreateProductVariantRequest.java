package com.example.solex_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateProductVariantRequest(

        @Schema(description = "Unique SKU code")
        @NotBlank(message = "SKU cannot be empty")
        @Size(max = 100, message = "SKU must not exceed 100 characters")
        String sku,

        @Schema(description = "Variant price")
        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        BigDecimal price,

        @Schema(description = "Variant image URL")
        String image,

        @Schema(description = "Variant size")
        String size,

        @Schema(description = "Variant name")
        String name,

        @Schema(description = "Is variant active (default true)")
        Boolean isActive
) {}
