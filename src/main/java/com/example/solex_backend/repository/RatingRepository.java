package com.example.solex_backend.repository;

import com.example.solex_backend.domain.Rating;
import com.example.solex_backend.domain.Restaurant;
import com.example.solex_backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByRestaurantAndUser(Restaurant restaurant, User user);

    List<Rating> findByRestaurantOrderByCreatedAtDesc(Restaurant restaurant);
}
