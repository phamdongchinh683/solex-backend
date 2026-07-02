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

import java.math.RoundingMode;

@Slf4j
@Component
@RequiredArgsConstructor
public class StripePaymentStrategy implements PaymentStrategy {

    private final StripeAccountService stripeAccountService;

    @Override
    public PaymentInitResult initiate(Order order, Payment payment, String clientIp) {
        String stripeAccountId = order.getRestaurant().getStripeAccountId();
        if (stripeAccountId == null) {
            throw new BusinessException("Restaurant has not connected a Stripe account. Please complete Stripe registration first.");
        }

        String stripeCustomerId = stripeAccountService.ensureStripeCustomer(order.getUser());

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(order.getTotalAmount().setScale(0, RoundingMode.HALF_UP).longValue())
                    .setCurrency("vnd")
                    .setCustomer(stripeCustomerId)
                    .setApplicationFeeAmount(payment.getCommissionAmount().setScale(0, RoundingMode.HALF_UP).longValue())
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
            throw new BusinessException("Cannot create Stripe payment: " + e.getMessage());
        }
    }

    @Override
    public boolean supports(PaymentMethod method) {
        return method == PaymentMethod.STRIPE;
    }
}
