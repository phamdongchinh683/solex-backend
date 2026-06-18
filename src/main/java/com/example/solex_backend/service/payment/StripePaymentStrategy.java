package com.example.solex_backend.service.payment;

import com.example.solex_backend.domain.Order;
import com.example.solex_backend.domain.Payment;
import com.example.solex_backend.exception.BusinessException;
import com.example.solex_backend.util.Enums.PaymentMethod;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StripePaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentInitResult initiate(Order order, Payment payment, String clientIp) {
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    // VND is a zero-decimal currency in Stripe — pass raw VND amount
                    .setAmount(order.getTotalAmount().longValueExact())
                    .setCurrency("vnd")
                    .putMetadata("orderId", order.getId().toString())
                    .putMetadata("orderCode", order.getOrderCode())
                    .putMetadata("transactionRef", payment.getTransactionRef())
                    .build();
            PaymentIntent intent = PaymentIntent.create(params);
            log.info("Stripe PaymentIntent created: id={}, status={}", intent.getId(), intent.getStatus());
            return new PaymentInitResult(intent.getId(), intent.getClientSecret(), null);
        } catch (StripeException e) {
            throw new BusinessException("Failed to create Stripe payment: " + e.getMessage());
        }
    }

    @Override
    public boolean supports(PaymentMethod method) {
        return method == PaymentMethod.STRIPE;
    }
}
