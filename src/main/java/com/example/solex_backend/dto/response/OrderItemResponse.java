package com.example.solex_backend.dto.response;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        String productName,
        String sku,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
}