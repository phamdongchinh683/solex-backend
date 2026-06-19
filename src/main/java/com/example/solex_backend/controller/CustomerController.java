package com.example.solex_backend.controller;

import com.example.solex_backend.domain.User;
import com.example.solex_backend.dto.ApiResponse;
import com.example.solex_backend.dto.request.*;
import com.example.solex_backend.dto.response.*;
import com.example.solex_backend.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Customer", description = "Customer endpoints")
@RestController
@RequestMapping("/api/v1/customer")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerController {

    private final AuthService authService;
    private final CustomerService customerService;
    private final AddressService addressService;
    private final CartService cartService;
    private final OrderService orderService;
    private final ShippingService shippingService;
    private final RatingService ratingService;
    private final ProductService productService;
    private final ProductVariantService productVariantService;
    private final RestaurantService restaurantService;


    @Operation(summary = "Register a new customer account")
    @PreAuthorize("permitAll()")
    @PostMapping("/sign-up")
    public ApiResponse<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.ok("OK", authService.signup(request));
    }


    @Operation(summary = "Get current user profile")
    @GetMapping("/profile")
    public ApiResponse<UserInfoResponse> getProfile(@AuthenticationPrincipal User user) {
        return ApiResponse.ok("OK", customerService.getProfile(user));
    }

    @Operation(summary = "Update user profile")
    @PutMapping("/profile")
    public ApiResponse<UserInfoResponse> updateProfile(
            @AuthenticationPrincipal User user,
            @RequestBody UpdateProfileRequest request) {
        return ApiResponse.ok("Profile updated", customerService.updateProfile(user, request));
    }


    @Operation(summary = "Get my addresses")
    @GetMapping("/addresses")
    public ApiResponse<List<AddressResponse>> getMyAddresses(@AuthenticationPrincipal User user) {
        return ApiResponse.ok("OK", addressService.getMyAddresses(user));
    }

    @Operation(summary = "Create new address")
    @PostMapping("/addresses")
    public ApiResponse<AddressResponse> createAddress(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateAddressRequest request) {
        return ApiResponse.ok("Address created", addressService.createAddress(user, request));
    }

    @Operation(summary = "Update address")
    @PutMapping("/addresses/{id}")
    public ApiResponse<AddressResponse> updateAddress(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody CreateAddressRequest request) {
        return ApiResponse.ok("Address updated", addressService.updateAddress(id, user, request));
    }

    @Operation(summary = "Delete address")
    @DeleteMapping("/addresses/{id}")
    public ApiResponse<Void> deleteAddress(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        addressService.deleteAddress(id, user);
        return ApiResponse.ok("Address deleted", null);
    }

    @Operation(summary = "Set default address")
    @PutMapping("/addresses/{id}/default")
    public ApiResponse<AddressResponse> setDefaultAddress(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ApiResponse.ok("Default address set", addressService.setDefaultAddress(id, user));
    }


    @Operation(summary = "List open restaurants")
    @GetMapping("/restaurants")
    public ApiResponse<List<RestaurantResponse>> getAllRestaurants(
            @Parameter(description = "Search by restaurant name")
            @RequestParam(required = false) String name) {
        return ApiResponse.ok("OK", restaurantService.getAllRestaurants(name));
    }

    @Operation(summary = "List categories in a restaurant")
    @GetMapping("/restaurants/{restaurantId}/categories")
    public ApiResponse<List<CategoryResponse>> getCategories(@PathVariable Long restaurantId) {
        return ApiResponse.ok("OK", restaurantService.getCategoriesForRestaurant(restaurantId));
    }

    @Operation(summary = "Get restaurant menu with optional filters")
    @GetMapping("/restaurants/{restaurantId}/menu")
    public ApiResponse<List<ProductResponse>> getMenu(
            @PathVariable Long restaurantId,
            @Parameter(description = "Filter by category") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Search by name")    @RequestParam(required = false) String search) {
        return ApiResponse.ok("OK", restaurantService.getMenuByRestaurantId(restaurantId, categoryId, search));
    }


    @Operation(summary = "Get restaurant ratings")
    @GetMapping("/restaurants/{restaurantId}/ratings")
    public ApiResponse<List<RatingResponse>> getRestaurantRatings(@PathVariable Long restaurantId) {
        return ApiResponse.ok("OK", ratingService.getRestaurantRatings(restaurantId));
    }

    @Operation(summary = "Create or update my restaurant rating")
    @PostMapping("/restaurants/{restaurantId}/ratings")
    public ApiResponse<RatingResponse> rateRestaurant(
            @PathVariable Long restaurantId,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateRatingRequest request) {
        return ApiResponse.ok("OK", ratingService.rateRestaurant(restaurantId, user, request));
    }


    @Operation(summary = "Get product detail by ID")
    @GetMapping("/products/{id}")
    public ApiResponse<ProductResponse> getProduct(@PathVariable Long id) {
        return ApiResponse.ok("OK", productService.getProductById(id));
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


    @Operation(summary = "Get current user's cart items")
    @GetMapping("/cart")
    public ApiResponse<List<CartItemResponse>> getCart(@AuthenticationPrincipal User user) {
        return ApiResponse.ok("OK", cartService.getCartItems(user));
    }

    @Operation(summary = "Add item to cart")
    @PostMapping("/cart/items")
    public ApiResponse<CartItemResponse> addToCart(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AddToCartRequest request) {
        return ApiResponse.ok("OK", cartService.addToCart(user, request));
    }

    @Operation(summary = "Update cart item quantity (+/-)")
    @PostMapping("/cart/items/{cartItemId}")
    public ApiResponse<CartItemResponse> updateCartItem(
            @AuthenticationPrincipal User user,
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        CartItemResponse response = cartService.updateCartItem(user, cartItemId, request.action());
        return response != null
                ? ApiResponse.ok("OK", response)
                : ApiResponse.ok("Removed", null);
    }

    @Operation(summary = "Remove item from cart")
    @DeleteMapping("/cart/items/{cartItemId}")
    public ApiResponse<Void> removeCartItem(
            @AuthenticationPrincipal User user,
            @PathVariable Long cartItemId) {
        cartService.deleteCartItem(user, cartItemId);
        return ApiResponse.ok("OK", null);
    }


    @Operation(summary = "Create order from cart")
    @PostMapping("/orders")
    public ApiResponse<OrderResponse> createOrder(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateOrderRequest request) {
        return ApiResponse.ok("OK", orderService.createOrder(user, request));
    }

    @Operation(summary = "Get my orders")
    @GetMapping("/orders")
    public ApiResponse<List<OrderResponse>> getMyOrders(@AuthenticationPrincipal User user) {
        return ApiResponse.ok("OK", orderService.getMyOrders(user));
    }

    @Operation(summary = "Get order detail")
    @GetMapping("/orders/{orderId}")
    public ApiResponse<OrderResponse> getOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal User user) {
        return ApiResponse.ok("OK", orderService.getOrderById(orderId, user));
    }

    @Operation(summary = "Calculate shipping fee")
    @GetMapping("/shipping/fee")
    public ApiResponse<ShippingFeeResponse> calculateFee(
            @RequestParam double restaurantLat,
            @RequestParam double restaurantLng,
            @RequestParam double userLat,
            @RequestParam double userLng) {
        return ApiResponse.ok("OK", shippingService.calculateShippingFee(restaurantLat, restaurantLng, userLat, userLng));
    }
}
