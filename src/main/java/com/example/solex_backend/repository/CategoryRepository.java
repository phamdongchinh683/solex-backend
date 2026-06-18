package com.example.solex_backend.repository;

import com.example.solex_backend.domain.Category;
import com.example.solex_backend.domain.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByRestaurantOrderByName(Restaurant restaurant);
    boolean existsByRestaurantAndName(Restaurant restaurant, String name);
}
