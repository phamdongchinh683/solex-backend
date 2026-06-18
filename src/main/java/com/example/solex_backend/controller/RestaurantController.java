package com.example.solex_backend.controller;

import com.example.solex_backend.dto.ApiResponse;
import com.example.solex_backend.dto.response.CategoryResponse;
import com.example.solex_backend.dto.response.ProductResponse;
import com.example.solex_backend.dto.response.RestaurantResponse;
import com.example.solex_backend.service.RestaurantService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Restaurants", description = "Restaurant and menu browsing")
@RestController
@RequestMapping("/api/v1/restaurants")
@SecurityRequirement(name = "bearerAuth")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @Operation(summary = "List open restaurants")
    @GetMapping
    public ApiResponse<List<RestaurantResponse>> getAllRestaurants(
            @Parameter(description = "Search by restaurant name")
            @RequestParam(required = false) String name) {
        return ApiResponse.ok("OK", restaurantService.getAllRestaurants(name));
    }

    @Operation(summary = "List categories available in this restaurant's menu")
    @GetMapping("/{id}/categories")
    public ApiResponse<List<CategoryResponse>> getCategories(@PathVariable Long id) {
        return ApiResponse.ok("OK", restaurantService.getCategoriesForRestaurant(id));
    }

    @Operation(summary = "Get menu with optional filters: category, price range, keyword, sort")
    @GetMapping("/{id}/menu")
    public ApiResponse<List<ProductResponse>> getMenu(
            @PathVariable Long id,
            @Parameter(description = "Filter by category") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Search by name") @RequestParam(required = false) String search) {
        return ApiResponse.ok("OK", restaurantService.getMenuByRestaurantId(id, categoryId, search));
    }
}