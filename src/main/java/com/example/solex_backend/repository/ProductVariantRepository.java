package com.example.solex_backend.repository;

import com.example.solex_backend.domain.Product;
import com.example.solex_backend.domain.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findByProduct(Product product);
    List<ProductVariant> findByProductAndIsActive(Product product, Boolean isActive);
    Optional<ProductVariant> findBySku(String sku);
}