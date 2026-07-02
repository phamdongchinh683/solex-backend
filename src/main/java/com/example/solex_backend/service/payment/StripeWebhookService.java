package com.example.solex_backend.service.payment;

import com.example.solex_backend.config.StripeConfig;
import com.example.solex_backend.domain.Payment;
import com.example.solex_backend.exception.BusinessException;
import com.example.solex_backend.service.OrderStatusService;
import com.example.solex_backend.util.Enums.PaymentStatus;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripeWebhookService {

    private final StripeConfig stripeConfig;
    private final PaymentService paymentService;
    private final OrderStatusService orderStatusService;

    @Transactional
    public void handleWebhook(String payload, String sigHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeConfig.getWebhookSecret());
        } catch (SignatureVerificationException e) {
            throw new BusinessException("Invalid Stripe webhook signature");
        }

        switch (event.getType()) {
            case "payment_intent.succeeded" -> {
                PaymentIntent intent = deserializeIntent(event);
                String transactionRef = intent.getId();
                log.info("Stripe payment succeeded: id={}", intent.getId());
                paymentService.findOptionalByTransactionRef(transactionRef).ifPresentOrElse(
                        payment -> {
                            paymentService.markSuccess(payment);
                            orderStatusService.confirmOrderByPayment(payment.getOrder().getId());
                        },
                        () -> log.warn("Stripe payment_intent.succeeded: no payment record found: intentId={}",
                                intent.getId()));
            }
            case "payment_intent.payment_failed" -> {
                PaymentIntent intent = deserializeIntent(event);
                String transactionRef = intent.getId();
                log.info("Stripe payment failed: id={}", intent.getId());
                paymentService.findOptionalByTransactionRef(transactionRef).ifPresentOrElse(
                        payment -> {
                            paymentService.markFailed(payment);
                            orderStatusService.cancelOrderByPayment(payment.getOrder().getId());
                        },
                        () -> log.warn(
                                "Stripe payment_intent.payment_failed: no payment record found: intentId={}",
                                intent.getId()));
            }
            case "charge.refunded" -> {
                Charge charge = (Charge) event.getDataObjectDeserializer()
                        .getObject()
                        .orElseGet(() -> {
                            try {
                                return event.getDataObjectDeserializer().deserializeUnsafe();
                            } catch (StripeException e) {
                                throw new BusinessException("Cannot read Stripe webhook data: " + e.getMessage());
                            }
                        });
                String paymentIntentId = charge.getPaymentIntent();
                log.info("Stripe charge refunded: chargeId={}, paymentIntentId={}", charge.getId(), paymentIntentId);
                paymentService.findOptionalByTransactionRef(paymentIntentId).ifPresent(payment -> {
                    if (!PaymentStatus.REFUNDED.name().equals(payment.getStatus())) {
                        paymentService.markRefunded(payment);
                    }
                });
            }
            default -> log.debug("Unhandled Stripe event: {}", event.getType());
        }
    }

    private PaymentIntent deserializeIntent(Event event) {
        return (PaymentIntent) event.getDataObjectDeserializer()
                .getObject()
                .orElseGet(() -> {
                    try {
                        return event.getDataObjectDeserializer().deserializeUnsafe();
                    } catch (StripeException e) {
                        throw new BusinessException("Cannot read Stripe webhook data: " + e.getMessage());
                    }
                });
    }
}
