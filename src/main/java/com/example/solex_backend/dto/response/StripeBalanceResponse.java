package com.example.solex_backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record StripeBalanceResponse(
        @Schema(description = "Amount available for payout (raw, zero-decimal VND)") long availableAmount,
        @Schema(description = "Amount still pending (raw, zero-decimal VND)") long pendingAmount,
        @Schema(description = "Currency code") String currency
) {}
