package com.example.solex_backend.controller;

import com.example.solex_backend.domain.User;
import com.example.solex_backend.dto.ApiResponse;
import com.example.solex_backend.dto.request.CreatePaymentRequest;
import com.example.solex_backend.dto.response.PaymentIntentResponse;
import com.example.solex_backend.dto.response.PaymentResponse;
import com.example.solex_backend.service.payment.PaymentService;
import com.example.solex_backend.service.payment.StripeWebhookService;
import com.example.solex_backend.service.payment.VNPayWebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Payments", description = "Payment processing — Stripe and VNPay")
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final StripeWebhookService stripeWebhookService;
    private final VNPayWebhookService vnPayWebhookService;

    @Operation(summary = "Initiate payment — returns clientSecret (Stripe) or redirectUrl (VNPay)")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<PaymentIntentResponse> initiatePayment(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid CreatePaymentRequest request,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok("Khởi tạo thanh toán thành công", paymentService.initiatePayment(user, request, getClientIp(httpRequest)));
    }

    @Operation(summary = "Get payment status by payment ID")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<PaymentResponse> getPayment(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ApiResponse.ok("Thành công", paymentService.getPaymentById(id, user));
    }

    @Operation(summary = "Stripe webhook — called by Stripe, no JWT required")
    @PostMapping("/stripe/webhook")
    public ResponseEntity<String> stripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        stripeWebhookService.handleWebhook(payload, sigHeader);
        return ResponseEntity.ok("OK");
    }

    @Operation(summary = "VNPay IPN — server-to-server callback from VNPay, no JWT required")
    @GetMapping("/vnpay/ipn")
    public ResponseEntity<Map<String, String>> vnpayIpn(@RequestParam Map<String, String> params) {
        return ResponseEntity.ok(vnPayWebhookService.handleIpn(params));
    }

    @Operation(summary = "VNPay return")
    @GetMapping("/vnpay/return")
    public ApiResponse<Map<String, String>> vnpayReturn(@RequestParam Map<String, String> params) {
        return ApiResponse.ok("Thành công", vnPayWebhookService.handleReturn(params));
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
