package com.example.solex_backend.dto.response;

import java.math.BigDecimal;

public record VariantResponse(
                Long id,
                BigDecimal price,
                String name,
                String size,
                String imageUrl) {
}
