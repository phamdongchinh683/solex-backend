package com.example.solex_backend.service.payment;

import com.example.solex_backend.domain.Order;
import com.example.solex_backend.domain.Payment;
import com.example.solex_backend.util.Enums.PaymentMethod;

public interface PaymentStrategy {
    PaymentInitResult initiate(Order order, Payment payment, String clientIp);
    boolean supports(PaymentMethod method);
}
