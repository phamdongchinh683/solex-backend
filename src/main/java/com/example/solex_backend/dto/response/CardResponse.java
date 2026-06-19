package com.example.solex_backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record CardResponse(
        @Schema(description = "Stripe PaymentMethod ID") String paymentMethodId,
        @Schema(description = "Card brand e.g. visa, mastercard") String brand,
        @Schema(description = "Last 4 digits") String last4,
        @Schema(description = "Expiry month") Long expMonth,
        @Schema(description = "Expiry year") Long expYear,
        @Schema(description = "Whether this is the default card") boolean isDefault
) {}
