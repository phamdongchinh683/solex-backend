package com.example.solex_backend.service.payment;

import com.example.solex_backend.domain.Payment;
import com.example.solex_backend.exception.BusinessException;
import com.example.solex_backend.service.OrderStatusService;
import com.example.solex_backend.util.Enums.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VNPayWebhookService {

    private final VNPayPaymentStrategy vnPayStrategy;
    private final PaymentService paymentService;
    private final OrderStatusService orderStatusService;

    @Transactional
    public Map<String, String> handleIpn(Map<String, String> params) {
        if (!vnPayStrategy.verifySignature(params)) {
            return Map.of("RspCode", "97", "Message", "Invalid signature");
        }

        String txnRef = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        String transactionStatus = params.get("vnp_TransactionStatus");

        Payment payment;
        try {
            payment = paymentService.findByTransactionRef(txnRef);
        } catch (Exception e) {
            return Map.of("RspCode", "01", "Message", "Order not found");
        }

        if (PaymentStatus.SUCCESS.name().equals(payment.getStatus())) {
            return Map.of("RspCode", "02", "Message", "Order already confirmed");
        }

        if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
            log.info("VNPay payment succeeded: txnRef={}", txnRef);
            paymentService.markSuccess(payment);
            orderStatusService.confirmOrderByPayment(payment.getOrder().getId());
        } else {
            log.info("VNPay payment failed: txnRef={}, code={}", txnRef, responseCode);
            paymentService.markFailed(payment);
            orderStatusService.cancelOrderByPayment(payment.getOrder().getId());
        }

        return Map.of("RspCode", "00", "Message", "Confirm Success");
    }

    @Transactional(readOnly = true)
    public Map<String, String> handleReturn(Map<String, String> params) {
        if (!vnPayStrategy.verifySignature(params)) {
            throw new BusinessException("Chữ ký VNPay trả về không hợp lệ");
        }
        String txnRef = params.get("vnp_TxnRef");
        Payment payment = paymentService.findByTransactionRef(txnRef);
        return Map.of(
                "status", payment.getStatus(),
                "orderCode", payment.getOrder().getOrderCode(),
                "amount", payment.getAmount().toPlainString());
    }
}
