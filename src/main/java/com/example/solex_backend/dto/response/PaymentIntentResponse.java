package com.example.solex_backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record PaymentIntentResponse(
        @Schema(description = "Payment record ID") Long paymentId,
        @Schema(description = "Transaction reference") String transactionRef,
        @Schema(description = "Stripe: pass this to Stripe.js on the frontend") String clientSecret,
        @Schema(description = "VNPay: redirect the browser to this URL") String redirectUrl,
        @Schema(description = "Payment method used") String method,
        @Schema(description = "Amount to pay") BigDecimal amount
) {}
