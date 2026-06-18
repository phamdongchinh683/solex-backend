package com.example.solex_backend.controller;

import com.example.solex_backend.domain.User;
import com.example.solex_backend.dto.ApiResponse;
import com.example.solex_backend.dto.request.CreateCategoryRequest;
import com.example.solex_backend.dto.response.CategoryResponse;
import com.example.solex_backend.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Categories", description = "Restaurant category management")
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Create category for operator's restaurant")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('OPERATOR')")
    @PostMapping
    public ApiResponse<CategoryResponse> createCategory(
            @AuthenticationPrincipal User operator,
            @RequestBody @Valid CreateCategoryRequest request) {
        return ApiResponse.ok("Category created", categoryService.createCategory(operator, request));
    }
}
