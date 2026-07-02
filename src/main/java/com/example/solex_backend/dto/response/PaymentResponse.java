package com.example.solex_backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        @Schema(description = "Payment ID") Long id,
        @Schema(description = "Order ID") Long orderId,
        @Schema(description = "Payment method: STRIPE / VNPAY / COD") String method,
        @Schema(description = "Payment status: PENDING / SUCCESS / FAILED / REFUNDED") String status,
        @Schema(description = "Amount") BigDecimal amount,
        @Schema(description = "Admin commission (20% of total, STRIPE only)") BigDecimal commissionAmount,
        @Schema(description = "Transaction reference") String transactionRef,
        @Schema(description = "Timestamp when payment was confirmed") LocalDateTime paidAt,
        @Schema(description = "Timestamp when payment was refunded") LocalDateTime refundedAt,
        @Schema(description = "Timestamp when payment record was created") LocalDateTime createdAt
) {}
