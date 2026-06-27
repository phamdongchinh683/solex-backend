package com.example.solex_backend.service;

import com.example.solex_backend.domain.Category;
import com.example.solex_backend.domain.Product;
import com.example.solex_backend.domain.Restaurant;
import com.example.solex_backend.domain.User;
import com.example.solex_backend.dto.request.CreateProductRequest;
import com.example.solex_backend.dto.response.ProductResponse;
import com.example.solex_backend.dto.response.ProductVariantResponse;
import com.example.solex_backend.exception.BusinessException;
import com.example.solex_backend.exception.ResourceNotFoundException;
import com.example.solex_backend.repository.CategoryRepository;
import com.example.solex_backend.repository.ProductRepository;
import com.example.solex_backend.repository.ProductVariantRepository;
import com.example.solex_backend.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository productVariantRepository;
    private final RestaurantRepository restaurantRepository;

    public ProductResponse createProduct(User operator, CreateProductRequest request) {
        if (!restaurantRepository.existsByIdAndOperator(request.restaurantId(), operator)) {
            throw new ResourceNotFoundException("Restaurant not found: " + request.restaurantId());
        }

        if (!categoryRepository.existsByIdAndRestaurant_Id(request.categoryId(), request.restaurantId())) {
            throw new BusinessException("Danh mục không thuộc về nhà hàng này");
        }

        Restaurant restaurant = restaurantRepository.getReferenceById(request.restaurantId());
        Category category = categoryRepository.getReferenceById(request.categoryId());

        Product product = Product.builder()
                .name(request.name())
                .description(request.description())
                .restaurant(restaurant)
                .category(category)
                .image(request.image())
                .basePrice(request.basePrice())
                .isActive(request.isActive() != null ? request.isActive() : true)
                .build();
        productRepository.save(product);

        return toProductResponse(product);
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        return toProductResponse(product);
    }

    private ProductResponse toProductResponse(Product p) {
        List<ProductVariantResponse> variants = productVariantRepository.findByProductAndIsActive(p, true).stream()
                .map(v -> new ProductVariantResponse(
                        v.getId(), v.getSku(),
                        v.getPrice(), v.getImage(), v.getSize(), v.getName(), v.getIsActive()))
                .collect(Collectors.toList());

        return new ProductResponse(
                p.getId(), p.getName(), p.getDescription(), p.getBasePrice(),
                p.getIsActive(), p.getCategory().getId(), p.getCategory().getName(),
                p.getImage());
    }

    
}
