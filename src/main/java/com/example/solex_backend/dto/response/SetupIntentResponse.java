package com.example.solex_backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record SetupIntentResponse(
        @Schema(description = "Stripe SetupIntent client secret — pass to Stripe.js to confirm card setup") String clientSecret
) {}
