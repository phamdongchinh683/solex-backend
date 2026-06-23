package com.example.solex_backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record CustomerVariantResponse(
        @Schema(description = "Variant ID")    Long id,
        @Schema(description = "Size / option") String size,
        @Schema(description = "Price")         BigDecimal price,
        @Schema(description = "Image URL")     String imageUrl
) {}
