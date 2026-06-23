package com.example.solex_backend.repository;

import com.example.solex_backend.domain.Coupon;
import com.example.solex_backend.domain.Restaurant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCode(String code);
    @Query("SELECT c FROM Coupon c WHERE c.restaurant = :restaurant AND c.id > :cursor ORDER BY c.id ASC")
    List<Coupon> findByRestaurantAfterCursor(@Param("restaurant") Restaurant restaurant,
                                             @Param("cursor") Long cursor,
                                             Pageable pageable);

    @Query("SELECT c FROM Coupon c WHERE c.restaurant = :restaurant " +
           "AND c.isActive = true AND c.startDate <= :now AND c.expiryDate >= :now AND c.id > :cursor ORDER BY c.id ASC")
    List<Coupon> findActiveByRestaurantAfterCursor(@Param("restaurant") Restaurant restaurant,
                                                   @Param("now") LocalDateTime now,
                                                   @Param("cursor") Long cursor,
                                                   Pageable pageable);
}
