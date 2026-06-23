package com.example.solex_backend.repository;

import com.example.solex_backend.domain.Category;
import com.example.solex_backend.domain.Restaurant;
import com.example.solex_backend.dto.response.CategorySummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<CategorySummary> findByRestaurant_IdOrderByName(Long restaurantId);
    boolean existsByRestaurantAndName(Restaurant restaurant, String name);
    boolean existsByIdAndRestaurant_Id(Long id, Long restaurantId);
}
