package com.example.solex_backend.repository;

import com.example.solex_backend.domain.Rating;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    @org.springframework.data.jpa.repository.Query("SELECT r FROM Rating r WHERE r.restaurant.id = :restaurantId AND r.id < :cursor ORDER BY r.id DESC")
    List<Rating> findByRestaurantBeforeCursor(@org.springframework.data.repository.query.Param("restaurantId") Long restaurantId, @org.springframework.data.repository.query.Param("cursor") Long cursor, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT r FROM Rating r WHERE r.restaurant.id = :restaurantId AND r.rating = :star AND r.id < :cursor ORDER BY r.id DESC")
    List<Rating> findByRestaurantAndStarBeforeCursor(@org.springframework.data.repository.query.Param("restaurantId") Long restaurantId, @org.springframework.data.repository.query.Param("star") int star, @org.springframework.data.repository.query.Param("cursor") Long cursor, Pageable pageable);
}
