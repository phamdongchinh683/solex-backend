package com.example.solex_backend.repository;

import com.example.solex_backend.domain.Category;
import com.example.solex_backend.domain.Restaurant;
import com.example.solex_backend.dto.response.CategorySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c.id AS id, c.name AS name, c.imageUrl AS imageUrl FROM Category c WHERE c.restaurant.id = :restaurantId ORDER BY c.id")
    List<CategorySummary> findSummariesByRestaurantId(@Param("restaurantId") Long restaurantId);

    boolean existsByRestaurantAndName(Restaurant restaurant, String name);

    boolean existsByIdAndRestaurant_Id(Long id, Long restaurantId);
}
