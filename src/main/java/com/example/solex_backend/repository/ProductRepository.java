package com.example.solex_backend.repository;

import com.example.solex_backend.domain.Category;
import com.example.solex_backend.domain.Product;
import com.example.solex_backend.domain.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    List<Product> findByRestaurant(Restaurant restaurant);
    List<Product> findByRestaurantAndIsActive(Restaurant restaurant, Boolean isActive);
    List<Product> findByCategory(Category category);
    List<Product> findByCategoryAndIsActive(Category category, Boolean isActive);
    List<Product> findByIsActive(Boolean isActive);
    List<Product> findByBasePriceBetween(BigDecimal min, BigDecimal max);
}