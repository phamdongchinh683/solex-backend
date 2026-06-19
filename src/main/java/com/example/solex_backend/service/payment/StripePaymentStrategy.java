package com.example.solex_backend.service.payment;

import com.example.solex_backend.domain.Order;
import com.example.solex_backend.domain.Payment;
import com.example.solex_backend.exception.BusinessException;
import com.example.solex_backend.util.Enums.PaymentMethod;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StripePaymentStrategy implements PaymentStrategy {

    private final StripeAccountService stripeAccountService;

    @Override
    public PaymentInitResult initiate(Order order, Payment payment, String clientIp) {
        String stripeAccountId = order.getRestaurant().getStripeAccountId();
        if (stripeAccountId == null) {
            throw new BusinessException("Restaurant has not connected a Stripe account. Please complete Stripe onboarding first.");
        }

        String stripeCustomerId = stripeAccountService.ensureStripeCustomer(order.getUser());

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(order.getTotalAmount().longValueExact())
                    .setCurrency("vnd")
                    .setCustomer(stripeCustomerId)
                    .setApplicationFeeAmount(payment.getCommissionAmount().longValueExact())
                    .setTransferData(
                            PaymentIntentCreateParams.TransferData.builder()
                                    .setDestination(stripeAccountId)
                                    .build()
                    )
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
