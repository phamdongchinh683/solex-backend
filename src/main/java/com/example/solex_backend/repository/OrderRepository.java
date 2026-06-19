package com.example.solex_backend.repository;

import com.example.solex_backend.domain.Order;
import com.example.solex_backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);

    Optional<Order> findByIdAndUser(Long id, User user);

    @Modifying
    @Query("UPDATE Order o SET o.rate = true WHERE o.id = :id")
    void markRated(@Param("id") Long id);
}