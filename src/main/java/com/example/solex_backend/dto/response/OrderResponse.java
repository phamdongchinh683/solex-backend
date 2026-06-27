package com.example.solex_backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponse(
                Long id,
                String orderCode,
                String status,
                BigDecimal subtotal,
                BigDecimal shippingFee,
                BigDecimal discountAmount,
                BigDecimal totalAmount,
                String note,
                LocalDateTime createdAt) {
}