package com.example.solex_backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResponse(
                Long id,
                String orderCode,
                String status,
                BigDecimal subtotal,
                BigDecimal shippingFee,
                BigDecimal discountAmount,
                BigDecimal totalAmount,
                String note,
                Boolean rate,
                LocalDateTime createdAt,
                double restaurantLatitude,
                double restaurantLongitude,
                double userLatitude,
                double userLongitude,
                String paymentMethod,
                String paymentStatus,
                String transactionRef,
                List<OrderItemResponse> items) {
}