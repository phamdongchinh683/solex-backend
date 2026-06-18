package com.example.solex_backend.service.payment;

public record PaymentInitResult(
        String transactionRef,
        String clientSecret,  // Stripe: client_secret for Stripe.js
        String redirectUrl    // VNPay: URL to redirect the browser to
) {}
