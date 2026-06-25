package com.example.solex_backend.repository;

import com.example.solex_backend.domain.Product;
import com.example.solex_backend.domain.ProductVariant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findByProductAndIsActive(Product product, Boolean isActive);

    @org.springframework.data.jpa.repository.Query("SELECT v FROM ProductVariant v WHERE v.product.id = :productId ORDER BY v.price ASC")
    List<ProductVariant> findVariantsByProductId(
            @org.springframework.data.repository.query.Param("productId") Long productId);

    @org.springframework.data.jpa.repository.Query("SELECT v FROM ProductVariant v WHERE v.product.id = :productId AND v.isActive = true AND v.id > :cursor ORDER BY v.id ASC")
    List<ProductVariant> findActiveByProductAfterCursor(
            @org.springframework.data.repository.query.Param("productId") Long productId,
            @org.springframework.data.repository.query.Param("cursor") Long cursor, Pageable pageable);

    Optional<ProductVariant> findByIdAndProduct_Id(Long id, Long productId);

    boolean existsByIdAndProduct_Id(Long id, Long productId);

    boolean existsBySku(String sku);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ProductVariant v SET v.isActive = false WHERE v.id = :id")
    void deactivateById(@Param("id") Long id);
}
