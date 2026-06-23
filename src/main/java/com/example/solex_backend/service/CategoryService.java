package com.example.solex_backend.service;

import com.example.solex_backend.domain.Category;
import com.example.solex_backend.domain.Restaurant;
import com.example.solex_backend.domain.User;
import com.example.solex_backend.dto.request.CreateCategoryRequest;
import com.example.solex_backend.dto.response.CategoryResponse;
import com.example.solex_backend.dto.response.CategorySummary;
import com.example.solex_backend.dto.response.SliceResponse;
import com.example.solex_backend.exception.BusinessException;
import com.example.solex_backend.exception.ResourceNotFoundException;
import com.example.solex_backend.repository.CategoryRepository;
import com.example.solex_backend.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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
            throw new BusinessException("Danh mục '" + request.name() + "' đã tồn tại trong nhà hàng này");
        }

        Category category = Category.builder()
                .restaurant(restaurant)
                .name(request.name())
                .imageUrl(request.imageUrl())
                .build();

        return toResponse(categoryRepository.save(category));
    }
    
    private CategoryResponse toResponse(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getImageUrl());
    }
}
