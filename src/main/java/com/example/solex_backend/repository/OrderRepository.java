package com.example.solex_backend.repository;

import com.example.solex_backend.domain.Order;
import com.example.solex_backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
}