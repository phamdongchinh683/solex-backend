package com.example.solex_backend.repository;

import com.example.solex_backend.domain.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductQueryRepository {

    private final ProductRepository productRepository;

    public List<Product> findByFilters(Long restaurantId, Long categoryId, String search, Long cursor, int limit) {
        Specification<Product> spec = forRestaurant(restaurantId).and(active()).and(idGreaterThan(cursor));

        if (categoryId != null)
            spec = spec.and(forCategory(categoryId));
        if (search != null && !search.isBlank())
            spec = spec.and(nameLike(search));

        return productRepository.findAll(spec, PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "id"))).getContent();
    }

    private Specification<Product> idGreaterThan(Long cursor) {
        return (root, q, cb) -> cursor == null ? cb.conjunction() : cb.greaterThan(root.get("id"), cursor);
    }

    private Specification<Product> forRestaurant(Long restaurantId) {
        return (root, q, cb) -> cb.equal(root.get("restaurant").get("id"), restaurantId);
    }

    private Specification<Product> active() {
        return (root, q, cb) -> cb.isTrue(root.get("isActive"));
    }

    private Specification<Product> forCategory(Long categoryId) {
        return (root, q, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    private Specification<Product> nameLike(String keyword) {
        return (root, q, cb) -> cb.like(
                cb.function("unaccent", String.class, cb.lower(root.get("name"))),
                cb.function("unaccent", String.class, cb.literal("%" + keyword.toLowerCase() + "%"))
        );
    }
}
