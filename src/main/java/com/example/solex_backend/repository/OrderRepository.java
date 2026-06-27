package com.example.solex_backend.repository;

import com.example.solex_backend.domain.Order;
import com.example.solex_backend.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.user = :user AND o.id < :cursor ORDER BY o.id DESC")
    List<Order> findByUserBeforeCursor(@Param("user") User user, @Param("cursor") Long cursor, Pageable pageable);
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id AND o.user = :user")
    Optional<Order> findByIdAndUser(@Param("id") Long id, @Param("user") User user);
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.restaurant.id = :restaurantId AND o.id < :cursor AND (:status IS NULL OR o.status = :status) ORDER BY o.id DESC")
    List<Order> findByRestaurantBeforeCursor(@Param("restaurantId") Long restaurantId, @Param("cursor") Long cursor, @Param("status") String status, Pageable pageable);

    @Modifying
    @Query("UPDATE Order o SET o.rate = true WHERE o.id = :id")
    void markRated(@Param("id") Long id);
}
