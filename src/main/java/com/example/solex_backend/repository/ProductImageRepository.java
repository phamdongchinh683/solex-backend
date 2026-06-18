package com.example.solex_backend.repository;

import com.example.solex_backend.domain.Product;
import com.example.solex_backend.domain.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProduct(Product product);
    List<ProductImage> findByProductAndIsPrimary(Product product, Boolean isPrimary);
}