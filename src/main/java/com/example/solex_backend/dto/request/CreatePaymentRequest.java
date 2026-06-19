package com.example.solex_backend.dto.request;

import com.example.solex_backend.util.Enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record CreatePaymentRequest(
        @Schema(description = "Order ID to pay for")
        @NotNull(message = "Order ID is required")
        Long orderId,

        @Schema(description = "Payment method: STRIPE or VNPAY")
        @NotNull(message = "Payment method is required")
        PaymentMethod method
) {}
