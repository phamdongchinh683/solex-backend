package com.example.solex_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateProductVariantRequest(

                @Schema(description = "Size label (e.g. S, M, L, XL)") @Size(max = 20, message = "Size must not exceed 20 characters") String size,

                @Schema(description = "Variant price") @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0") BigDecimal price,

                @Schema(description = "Variant image URL") String image,

                @Schema(description = "Variant name") String name,

                @Schema(description = "Is variant active") Boolean isActive) {
}
