package com.example.solex_backend.repository;

import com.example.solex_backend.domain.Order;
import com.example.solex_backend.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByTransactionRef(String transactionRef);
    Optional<Payment> findTopByOrderOrderByIdDesc(Order order);
}
