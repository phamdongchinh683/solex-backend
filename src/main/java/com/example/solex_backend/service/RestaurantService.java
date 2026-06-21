package com.example.solex_backend.service;

import com.example.solex_backend.domain.Category;
import com.example.solex_backend.domain.Product;
import com.example.solex_backend.domain.Restaurant;
import com.example.solex_backend.domain.User;
import com.example.solex_backend.dto.request.CreateCategoryRequest;
import com.example.solex_backend.dto.request.UpdateRestaurantRequest;
import com.example.solex_backend.dto.response.CategoryResponse;
import com.example.solex_backend.dto.response.ProductResponse;
import com.example.solex_backend.dto.response.ProductVariantResponse;
import com.example.solex_backend.dto.response.RestaurantResponse;
import com.example.solex_backend.exception.BusinessException;
import com.example.solex_backend.exception.ResourceNotFoundException;
import com.example.solex_backend.repository.CategoryRepository;
import com.example.solex_backend.repository.ProductImageRepository;
import com.example.solex_backend.repository.ProductQueryRepository;
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
public class RestaurantService {

        private final RestaurantRepository restaurantRepository;
        private final CategoryRepository categoryRepository;
        private final ProductQueryRepository productQueryRepository;
        private final ProductImageRepository productImageRepository;
        private final ProductVariantRepository productVariantRepository;

        public List<RestaurantResponse> getAllRestaurants(String name) {
                List<Restaurant> restaurants = (name != null && !name.isBlank())
                                ? restaurantRepository.searchOpenByName(name)
                                : restaurantRepository.findByIsOpen(true);
                return restaurants.stream()
                                .map(this::toRestaurantResponse)
                                .collect(Collectors.toList());
        }

        public RestaurantResponse getRestaurantById(Long id) {
                Restaurant restaurant = restaurantRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + id));
                return toRestaurantResponse(restaurant);
        }

        public List<CategoryResponse> getCategoriesForRestaurant(Long id) {
                Restaurant restaurant = restaurantRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + id));
                return categoryRepository.findByRestaurantOrderByName(restaurant).stream()
                                .map(c -> new CategoryResponse(c.getId(), c.getName(), c.getImageUrl()))
                                .collect(Collectors.toList());
        }

        public CategoryResponse createCategory(User operator, CreateCategoryRequest request) {
                Restaurant restaurant = restaurantRepository.findByOperator(operator)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Restaurant not found for this operator"));

                if (categoryRepository.existsByRestaurantAndName(restaurant, request.name())) {
                        throw new BusinessException(
                                        "Category '" + request.name() + "' already exists in your restaurant");
                }

                Category category = Category.builder()
                                .restaurant(restaurant)
                                .name(request.name())
                                .imageUrl(request.imageUrl())
                                .isActive(1)
                                .build();

                Category saved = categoryRepository.save(category);
                return new CategoryResponse(saved.getId(), saved.getName(), saved.getImageUrl());
        }

        public List<ProductResponse> getMenuByRestaurantId(Long id, Long categoryId, String search) {
                Restaurant restaurant = restaurantRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + id));
                return productQueryRepository.findByFilters(restaurant, categoryId, search)
                                .stream()
                                .map(this::toProductResponse)
                                .collect(Collectors.toList());
        }

        private RestaurantResponse toRestaurantResponse(Restaurant r) {
                return new RestaurantResponse(
                                r.getId(), r.getName(), r.getDescription(), r.getPhone(),
                                r.getAddressDetail(), r.getLongitude(), r.getLatitude(),
                                r.getStar1(), r.getStar2(), r.getStar3(), r.getStar4(), r.getStar5(),
                                r.getIsOpen(), r.getImageUrl());
        }

        private ProductResponse toProductResponse(Product p) {
                List<String> images = productImageRepository.findByProduct(p).stream()
                                .map(img -> img.getUrl())
                                .collect(Collectors.toList());

                List<ProductVariantResponse> variants = productVariantRepository.findByProduct(p).stream()
                                .filter(v -> Boolean.TRUE.equals(v.getIsActive()))
                                .map(v -> new ProductVariantResponse(
                                                v.getId(), v.getSku(), v.getSize(),
                                                v.getPrice(), v.getStock(), v.getImageUrl(), v.getIsActive()))
                                .collect(Collectors.toList());

                return new ProductResponse(
                                p.getId(), p.getName(), p.getDescription(), p.getBasePrice(),
                                p.getIsActive(), p.getCategory().getId(), p.getCategory().getName(),
                                images, variants);
        }

        @Transactional
        public RestaurantResponse updateRestaurant(User operator, UpdateRestaurantRequest request) {
                Restaurant restaurant = restaurantRepository.findByOperator(operator)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Restaurant not found for this operator"));

                if (request.name() != null)          restaurant.setName(request.name());
                if (request.description() != null)   restaurant.setDescription(request.description());
                if (request.phone() != null)         restaurant.setPhone(request.phone());
                if (request.addressDetail() != null) restaurant.setAddressDetail(request.addressDetail());
                if (request.longitude() != null)     restaurant.setLongitude(request.longitude());
                if (request.latitude() != null)      restaurant.setLatitude(request.latitude());
                if (request.imageUrl() != null)      restaurant.setImageUrl(request.imageUrl());

                return toRestaurantResponse(restaurantRepository.save(restaurant));
        }

        @Transactional
        public void updateIsOpenRestaurant(Long id, User operator, boolean status) {
                int rows = restaurantRepository.updateStatusByOperator(id, operator.getId(), status);
                if (rows == 0)
                        throw new ResourceNotFoundException("Not change");
        }
}
