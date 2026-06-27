package com.example.solex_backend.service.payment;

import com.example.solex_backend.domain.Order;
import com.example.solex_backend.domain.Payment;
import com.example.solex_backend.util.Enums.PaymentMethod;
import org.springframework.stereotype.Component;

@Component
public class CODPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentInitResult initiate(Order order, Payment payment, String clientIp) {
        return new PaymentInitResult(payment.getTransactionRef(), null, null);
    }

    @Override
    public boolean supports(PaymentMethod method) {
        return method == PaymentMethod.COD;
    }
}