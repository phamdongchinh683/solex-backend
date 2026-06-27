package com.example.solex_backend.service.payment;

import com.example.solex_backend.config.StripeConfig;
import com.example.solex_backend.domain.Payment;
import com.example.solex_backend.exception.BusinessException;
import com.example.solex_backend.service.OrderStatusService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
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
            throw new BusinessException("Chữ ký webhook Stripe không hợp lệ");
        }

        switch (event.getType()) {
            case "payment_intent.succeeded" -> {
                PaymentIntent intent = deserializeIntent(event);
                String transactionRef = intent.getMetadata().get("transactionRef");
                log.info("Stripe payment succeeded: id={}", intent.getId());
                Payment payment = paymentService.findByTransactionRef(transactionRef);
                paymentService.markSuccess(payment, intent.getId());
                orderStatusService.confirmOrderByPayment(payment.getOrder().getId());
            }
            case "payment_intent.payment_failed" -> {
                PaymentIntent intent = deserializeIntent(event);
                String transactionRef = intent.getMetadata().get("transactionRef");
                log.info("Stripe payment failed: id={}", intent.getId());
                Payment payment = paymentService.findByTransactionRef(transactionRef);
                paymentService.markFailed(payment, intent.getId());
                orderStatusService.cancelOrderByPayment(payment.getOrder().getId());
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
                        throw new BusinessException("Không thể đọc dữ liệu webhook Stripe: " + e.getMessage());
                    }
                });
    }
}
