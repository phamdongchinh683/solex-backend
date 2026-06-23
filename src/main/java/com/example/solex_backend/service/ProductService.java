package com.example.solex_backend.service;

import com.example.solex_backend.domain.Category;
import com.example.solex_backend.domain.Product;
import com.example.solex_backend.domain.ProductImage;
import com.example.solex_backend.domain.Restaurant;
import com.example.solex_backend.domain.User;
import com.example.solex_backend.dto.request.CreateProductRequest;
import com.example.solex_backend.dto.response.ProductResponse;
import com.example.solex_backend.dto.response.ProductVariantResponse;
import com.example.solex_backend.exception.BusinessException;
import com.example.solex_backend.exception.ResourceNotFoundException;
import com.example.solex_backend.repository.CategoryRepository;
import com.example.solex_backend.repository.ProductImageRepository;
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
    private final ProductImageRepository productImageRepository;
    private final ProductVariantRepository productVariantRepository;
    private final RestaurantRepository restaurantRepository;

    // Rule 1: existsByIdAndOperator adds missing operator-ownership check on restaurantId
    // Rule 2: getReferenceById replaces findById used only for FK assignment;
    //         existsByIdAndRestaurant_Id replaces findById on category to avoid lazy-loading restaurant
    public ProductResponse createProduct(User operator, CreateProductRequest request) {
        if (!restaurantRepository.existsByIdAndOperator(request.restaurantId(), operator)) {
            throw new ResourceNotFoundException("Restaurant not found: " + request.restaurantId());
        }

        if (!categoryRepository.existsByIdAndRestaurant_Id(request.categoryId(), request.restaurantId())) {
            throw new BusinessException("Category does not belong to this restaurant");
        }

        Restaurant restaurant = restaurantRepository.getReferenceById(request.restaurantId());
        Category category = categoryRepository.getReferenceById(request.categoryId());

        Product product = Product.builder()
                .name(request.name())
                .description(request.description())
                .restaurant(restaurant)
                .category(category)
                .basePrice(request.basePrice())
                .isActive(request.isActive() != null ? request.isActive() : true)
                .build();
        productRepository.save(product);

        if (request.images() != null) {
            for (String url : request.images()) {
                ProductImage image = ProductImage.builder()
                        .product(product)
                        .url(url)
                        .isPrimary(false)
                        .sortOrder(0)
                        .build();
                productImageRepository.save(image);
            }
        }

        return toProductResponse(product);
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        return toProductResponse(product);
    }

    private ProductResponse toProductResponse(Product p) {
        List<String> images = productImageRepository.findByProduct(p).stream()
                .map(ProductImage::getUrl)
                .collect(Collectors.toList());

        List<ProductVariantResponse> variants = productVariantRepository.findByProductAndIsActive(p, true).stream()
                .map(v -> new ProductVariantResponse(
                        v.getId(), v.getSku(), v.getSize(),
                        v.getPrice(), v.getStock(), v.getImageUrl(), v.getIsActive()
                ))
                .collect(Collectors.toList());

        return new ProductResponse(
                p.getId(), p.getName(), p.getDescription(), p.getBasePrice(),
                p.getIsActive(), p.getCategory().getId(), p.getCategory().getName(),
                images, variants
        );
    }
}
