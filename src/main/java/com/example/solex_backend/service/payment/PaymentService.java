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
import com.example.solex_backend.util.Enums.PaymentMethod;
import com.example.solex_backend.util.Enums.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private static final BigDecimal STRIPE_COMMISSION_RATE = new BigDecimal("0.20");

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final List<PaymentStrategy> strategies;

    public PaymentIntentResponse initiatePayment(User user, CreatePaymentRequest request, String clientIp) {
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + request.orderId()));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Bạn không có quyền thanh toán cho đơn hàng này");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException("Chỉ các đơn hàng ở trạng thái CHỜ XỬ LÝ mới có thể thanh toán");
        }

        boolean hasPending = paymentRepository.findByOrder(order).stream()
                .anyMatch(p -> PaymentStatus.PENDING.name().equals(p.getStatus()));
        if (hasPending) {
            throw new BusinessException("Đã có giao dịch thanh toán đang chờ xử lý cho đơn hàng này");
        }

        String transactionRef = UUID.randomUUID().toString().replace("-", "").substring(0, 20);

        BigDecimal commissionAmount = request.method() == PaymentMethod.STRIPE
                ? order.getTotalAmount().multiply(STRIPE_COMMISSION_RATE)
                : null;

        Payment payment = Payment.builder()
                .order(order)
                .method(request.method().name())
                .status(PaymentStatus.PENDING.name())
                .amount(order.getTotalAmount())
                .commissionAmount(commissionAmount)
                .transactionRef(transactionRef)
                .build();
        payment = paymentRepository.saveAndFlush(payment);

        PaymentStrategy strategy = strategies.stream()
                .filter(s -> s.supports(request.method()))
                .findFirst()
                .orElseThrow(
                        () -> new BusinessException("Phương thức thanh toán không được hỗ trợ: " + request.method()));

        PaymentInitResult result = strategy.initiate(order, payment, clientIp);

        if (result.transactionRef() != null && !result.transactionRef().equals(transactionRef)) {
            payment.setTransactionRef(result.transactionRef());
            paymentRepository.saveAndFlush(payment);
        }

        return new PaymentIntentResponse(
                payment.getId(),
                payment.getTransactionRef(),
                result.clientSecret(),
                result.redirectUrl(),
                request.method().name(),
                order.getTotalAmount());
    }

    public PaymentResponse getPaymentById(Long id, User user) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));
        if (!payment.getOrder().getUser().getId().equals(user.getId())) {
            throw new BusinessException("Bạn không có quyền xem giao dịch thanh toán này");
        }
        return toPaymentResponse(payment);
    }

    public Payment findByTransactionRef(String ref) {
        return paymentRepository.findByTransactionRef(ref)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for ref: " + ref));
    }

    public java.util.Optional<Payment> findOptionalByTransactionRef(String ref) {
        return paymentRepository.findByTransactionRef(ref);
    }

    public void markSuccess(Payment payment) {
        payment.setStatus(PaymentStatus.SUCCESS.name());
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);
    }

    public void markFailed(Payment payment) {
        payment.setStatus(PaymentStatus.FAILED.name());
        paymentRepository.save(payment);
    }

    private PaymentResponse toPaymentResponse(Payment p) {
        return new PaymentResponse(
                p.getId(),
                p.getOrder().getId(),
                p.getMethod(),
                p.getStatus(),
                p.getAmount(),
                p.getCommissionAmount(),
                p.getTransactionRef(),
                p.getPaidAt(),
                p.getCreatedAt());
    }
}