package com.example.solex_backend.dto.response;


import java.math.BigDecimal;

public record CartItemResponse(
        Long id,
        ProductResponse product,
        ProductVariantResponse variant,
        Integer quantity,
        BigDecimal itemPrice
) {
}