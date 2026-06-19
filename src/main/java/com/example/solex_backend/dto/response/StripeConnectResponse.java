package com.example.solex_backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record StripeConnectResponse(
        @Schema(description = "Stripe Connect account ID") String accountId,
        @Schema(description = "Stripe onboarding URL — redirect operator to this URL") String onboardingUrl
) {}
