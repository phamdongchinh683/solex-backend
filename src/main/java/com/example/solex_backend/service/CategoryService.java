package com.example.solex_backend.service;

import com.example.solex_backend.domain.Category;
import com.example.solex_backend.domain.Restaurant;
import com.example.solex_backend.domain.User;
import com.example.solex_backend.dto.request.CreateCategoryRequest;
import com.example.solex_backend.dto.response.CategoryResponse;
import com.example.solex_backend.exception.BusinessException;
import com.example.solex_backend.exception.ResourceNotFoundException;
import com.example.solex_backend.repository.CategoryRepository;
import com.example.solex_backend.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final RestaurantRepository restaurantRepository;

    public CategoryResponse createCategory(User operator, CreateCategoryRequest request) {
        Restaurant restaurant = restaurantRepository.findByOperator(operator)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found for this operator"));

        if (categoryRepository.existsByRestaurantAndName(restaurant, request.name())) {
            throw new BusinessException("Category '" + request.name() + "' already exists in this restaurant");
        }

        Category category = Category.builder()
                .restaurant(restaurant)
                .name(request.name())
                .imageUrl(request.imageUrl())
                .build();

        return toResponse(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoriesByRestaurant(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + restaurantId));

        return categoryRepository.findByRestaurantOrderByName(restaurant).stream()
                .filter(c -> Integer.valueOf(1).equals(c.getIsActive()))
                .map(this::toResponse)
                .toList();
    }

    private CategoryResponse toResponse(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getImageUrl());
    }
}
