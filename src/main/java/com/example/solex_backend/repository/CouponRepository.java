package com.example.solex_backend.repository;

import com.example.solex_backend.domain.Coupon;
import com.example.solex_backend.domain.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCode(String code);

    List<Coupon> findByRestaurant(Restaurant restaurant);

    @Query("SELECT c FROM Coupon c WHERE c.restaurant = :restaurant " +
           "AND c.isActive = true AND c.startDate <= :now AND c.expiryDate >= :now")
    List<Coupon> findActiveByRestaurant(@Param("restaurant") Restaurant restaurant,
                                       @Param("now") LocalDateTime now);
}
