package com.example.solex_backend.service.payment;

import com.example.solex_backend.domain.Order;
import com.example.solex_backend.domain.Payment;
import com.example.solex_backend.domain.User;
import com.example.solex_backend.dto.request.CreatePaymentRequest;
import com.example.solex_backend.dto.response.PaymentIntentResponse;
import com.example.solex_backend.dto.response.PaymentResponse;
import com.example.solex_backend.exception.BusinessException;
import com.example.solex_backend.exception.ResourceNotFoundException;
import com.example.solex_backend.repository.OrderRepository;
import com.example.solex_backend.repository.PaymentRepository;
import com.example.solex_backend.service.CouponService;
import com.example.solex_backend.util.Enums.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final CouponService couponService;
    private final List<PaymentStrategy> strategies;

    public PaymentIntentResponse initiatePayment(User user, CreatePaymentRequest request, String clientIp) {
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + request.orderId()));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new BusinessException("You are not allowed to pay for this order");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException("Only PENDING orders can be paid");
        }

        boolean hasPending = paymentRepository.findByOrder(order).stream()
                .anyMatch(p -> PaymentStatus.PENDING.name().equals(p.getStatus()));
        if (hasPending) {
            throw new BusinessException("A pending payment already exists for this order");
        }

        if (request.couponId() != null) {
            couponService.applyToOrder(request.couponId(), order);
        }

        String transactionRef = UUID.randomUUID().toString().replace("-", "").substring(0, 20);

        Payment payment = Payment.builder()
                .order(order)
                .method(request.method().name())
                .status(PaymentStatus.PENDING.name())
                .amount(order.getTotalAmount())
                .transactionRef(transactionRef)
                .build();
        paymentRepository.save(payment);

        PaymentStrategy strategy = strategies.stream()
                .filter(s -> s.supports(request.method()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Unsupported payment method: " + request.method()));

        PaymentInitResult result = strategy.initiate(order, payment, clientIp);

        // Stripe returns its own PaymentIntent ID as transactionRef
        if (result.transactionRef() != null && !result.transactionRef().equals(transactionRef)) {
            payment.setTransactionRef(result.transactionRef());
            paymentRepository.save(payment);
        }

        return new PaymentIntentResponse(
                payment.getId(),
                payment.getTransactionRef(),
                result.clientSecret(),
                result.redirectUrl(),
                request.method().name(),
                order.getTotalAmount()
        );
    }

    public PaymentResponse getPaymentById(Long id, User user) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));
        if (!payment.getOrder().getUser().getId().equals(user.getId())) {
            throw new BusinessException("You are not allowed to view this payment");
        }
        return toPaymentResponse(payment);
    }

    public Payment findByTransactionRef(String ref) {
        return paymentRepository.findByTransactionRef(ref)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for ref: " + ref));
    }

    public void markSuccess(Payment payment, String gatewayResponse) {
        payment.setStatus(PaymentStatus.SUCCESS.name());
        payment.setGatewayResponse(gatewayResponse);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);
    }

    public void markFailed(Payment payment, String gatewayResponse) {
        payment.setStatus(PaymentStatus.FAILED.name());
        payment.setGatewayResponse(gatewayResponse);
        paymentRepository.save(payment);
    }

    private PaymentResponse toPaymentResponse(Payment p) {
        return new PaymentResponse(
                p.getId(),
                p.getOrder().getId(),
                p.getMethod(),
                p.getStatus(),
                p.getAmount(),
                p.getTransactionRef(),
                p.getPaidAt(),
                p.getCreatedAt()
        );
    }
}
