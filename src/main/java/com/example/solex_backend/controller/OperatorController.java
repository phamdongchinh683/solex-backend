package com.example.solex_backend.controller;

import com.example.solex_backend.domain.User;
import com.example.solex_backend.dto.ApiResponse;
import com.example.solex_backend.dto.request.*;
import com.example.solex_backend.dto.response.*;
import com.example.solex_backend.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Operator", description = "Operator management endpoints")
@RestController
@RequestMapping("/api/v1/operator")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('OPERATOR')")
public class OperatorController {

    private final AuthService authService;
    private final OrderService orderService;
    private final OrderStatusService orderStatusService;
    private final RestaurantService restaurantService;
    private final ProductService productService;
    private final ProductVariantService productVariantService;
    private final CategoryService categoryService;

    // ── Auth ──────────────────────────────────────────────────────────────────

    @Operation(summary = "Operator sign-up — creates operator account and restaurant in one request")
    @PreAuthorize("permitAll()")
    @PostMapping("/sign-up")
    public ApiResponse<AuthResponse> signUp(@Valid @RequestBody OperatorSignupRequest request) {
        return ApiResponse.ok("OK", authService.signupOperator(request));
    }

    // ── Orders ────────────────────────────────────────────────────────────────

    @Operation(summary = "Get order detail (operator view)")
    @GetMapping("/orders/{id}")
    public ApiResponse<OrderResponse> getOrder(@PathVariable Long id) {
        return ApiResponse.ok("OK", orderService.getOrderForOperator(id));
    }

    @Operation(summary = "Confirm order")
    @PutMapping("/orders/{id}/confirm")
    public ApiResponse<Void> confirmOrder(
            @AuthenticationPrincipal User operator,
            @PathVariable Long id) {
        orderStatusService.confirmOrder(id, operator);
        return ApiResponse.ok("Order confirmed", null);
    }

    @Operation(summary = "Advance order to next status")
    @PutMapping("/orders/{id}/advance")
    public ApiResponse<Void> advanceOrder(
            @AuthenticationPrincipal User operator,
            @PathVariable Long id) {
        orderStatusService.advanceOrder(id, operator);
        return ApiResponse.ok("Order status advanced", null);
    }

    @Operation(summary = "Cancel order")
    @PutMapping("/orders/{id}/cancel")
    public ApiResponse<Void> cancelOrder(
            @AuthenticationPrincipal User operator,
            @PathVariable Long id,
            @RequestParam String reason) {
        orderStatusService.cancelOrder(id, operator, reason);
        return ApiResponse.ok("Order cancelled", null);
    }

    // ── Restaurant ────────────────────────────────────────────────────────────

    @Operation(summary = "Update restaurant info (all fields optional)")
    @PatchMapping("/restaurant")
    public ApiResponse<RestaurantResponse> updateRestaurant(
            @AuthenticationPrincipal User operator,
            @Valid @RequestBody UpdateRestaurantRequest request) {
        return ApiResponse.ok("Restaurant updated", restaurantService.updateRestaurant(operator, request));
    }

    @Operation(summary = "Update restaurant open/close status")
    @PatchMapping("/restaurant/status")
    public ApiResponse<Void> updateRestaurantStatus(
            @AuthenticationPrincipal User operator,
            @RequestBody UpdateStatusRestaurantRequest request) {
        restaurantService.updateIsOpenRestaurant(request.restaurantId(), operator, request.status());
        return ApiResponse.ok(request.status() ? "Restaurant is now open" : "Restaurant is now closed", null);
    }

    // ── Categories ────────────────────────────────────────────────────────────

    @Operation(summary = "Create category for operator's restaurant")
    @PostMapping("/categories")
    public ApiResponse<CategoryResponse> createCategory(
            @AuthenticationPrincipal User operator,
            @Valid @RequestBody CreateCategoryRequest request) {
        return ApiResponse.ok("Category created", categoryService.createCategory(operator, request));
    }

    // ── Products ──────────────────────────────────────────────────────────────

    @Operation(summary = "Create product")
    @PostMapping("/products")
    public ApiResponse<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        return ApiResponse.ok("OK", productService.createProduct(request));
    }

    @Operation(summary = "Get product detail by ID")
    @GetMapping("/products/{id}")
    public ApiResponse<ProductResponse> getProduct(@PathVariable Long id) {
        return ApiResponse.ok("OK", productService.getProductById(id));
    }

    // ── Product Variants ──────────────────────────────────────────────────────

    @Operation(summary = "Create variant for a product")
    @PostMapping("/products/{id}/variants")
    public ApiResponse<ProductVariantResponse> createVariant(
            @PathVariable Long id,
            @Valid @RequestBody CreateProductVariantRequest request) {
        return ApiResponse.ok("OK", productVariantService.createVariant(id, request));
    }

    @Operation(summary = "List all variants of a product")
    @GetMapping("/products/{id}/variants")
    public ApiResponse<List<ProductVariantResponse>> getVariants(@PathVariable Long id) {
        return ApiResponse.ok("OK", productVariantService.getVariantsByProduct(id));
    }

    @Operation(summary = "Get a single product variant")
    @GetMapping("/products/{id}/variants/{variantId}")
    public ApiResponse<ProductVariantResponse> getVariant(
            @PathVariable Long id,
            @PathVariable Long variantId) {
        return ApiResponse.ok("OK", productVariantService.getVariantById(id, variantId));
    }

    @Operation(summary = "Update a product variant")
    @PutMapping("/products/{id}/variants/{variantId}")
    public ApiResponse<ProductVariantResponse> updateVariant(
            @PathVariable Long id,
            @PathVariable Long variantId,
            @Valid @RequestBody UpdateProductVariantRequest request) {
        return ApiResponse.ok("OK", productVariantService.updateVariant(id, variantId, request));
    }

    @Operation(summary = "Deactivate a product variant")
    @DeleteMapping("/products/{id}/variants/{variantId}")
    public ApiResponse<Void> deleteVariant(
            @PathVariable Long id,
            @PathVariable Long variantId) {
        productVariantService.deleteVariant(id, variantId);
        return ApiResponse.ok("Variant deactivated", null);
    }
}
