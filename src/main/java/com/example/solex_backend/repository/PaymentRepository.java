package com.example.solex_backend.repository;

import com.example.solex_backend.domain.Order;
import com.example.solex_backend.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByTransactionRef(String transactionRef);
    List<Payment> findByOrder(Order order);
}
