package com.example.solex_backend.repository;

import com.example.solex_backend.domain.Product;
import com.example.solex_backend.domain.Restaurant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductQueryRepository {

    private final ProductRepository productRepository;

    public List<Product> findByFilters(
            Restaurant restaurant,
                    Long categoryId,
            String search) {

        Specification<Product> spec = forRestaurant(restaurant).and(active());

        if (categoryId != null)
            spec = spec.and(forCategory(categoryId));
        if (search != null && !search.isBlank())
            spec = spec.and(nameLike(search));

        return productRepository.findAll(spec);
    }

    private Specification<Product> forRestaurant(Restaurant restaurant) {
        return (root, q, cb) -> cb.equal(root.get("restaurant"), restaurant);
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
