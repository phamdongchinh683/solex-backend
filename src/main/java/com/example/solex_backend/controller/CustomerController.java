package com.example.solex_backend.controller;

import com.example.solex_backend.domain.User;
import com.example.solex_backend.dto.ApiResponse;
import com.example.solex_backend.dto.request.*;
import com.example.solex_backend.dto.response.*;
import com.example.solex_backend.service.*;
import com.example.solex_backend.service.payment.StripeCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
    private final ProductVariantService productVariantService;
    private final RestaurantService restaurantService;
    private final CouponService couponService;
    private final StripeCardService stripeCardService;

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
            @Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.ok("Profile updated OK", customerService.updateProfile(user, request));
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
        return ApiResponse.ok("Address has been created", addressService.createAddress(user, request));
    }

    @Operation(summary = "Update address")
    @PutMapping("/addresses/{id}")
    public ApiResponse<AddressResponse> updateAddress(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody CreateAddressRequest request) {
        return ApiResponse.ok("Address has been updated", addressService.updateAddress(id, user, request));
    }

    @Operation(summary = "Delete address")
    @DeleteMapping("/addresses/{id}")
    public ApiResponse<Void> deleteAddress(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        addressService.deleteAddress(id, user);
        return ApiResponse.ok("Address has been deleted", null);
    }

    @Operation(summary = "Set default address")
    @PutMapping("/addresses/{id}/default")
    public ApiResponse<AddressResponse> setDefaultAddress(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ApiResponse.ok("Default address has been set", addressService.setDefaultAddress(id, user));
    }

    @Operation(summary = "Search nearby open restaurants sorted by distance")
    @GetMapping("/restaurants/nearby")
    public ApiResponse<SliceResponse<RestaurantNearbyResponse>> getNearbyRestaurants(
            @RequestParam double lat,
            @RequestParam double lng,
            @Parameter(description = "Search radius in kilometres") @RequestParam(defaultValue = "12.0") double radius,
            @RequestParam(defaultValue = "0") int cursor,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok("OK", restaurantService.getNearbyRestaurants(lat, lng, radius, cursor, size));
    }

    @Operation(summary = "List open restaurants")
    @GetMapping("/restaurants")
    public ApiResponse<SliceResponse<RestaurantResponse>> getAllRestaurants(
            @Parameter(description = "Search by restaurant name") @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") Long cursor,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok("OK", restaurantService.getAllRestaurants(name, cursor, size));
    }

    @Operation(summary = "Get restaurant detail by ID")
    @GetMapping("/restaurants/{id}")
    public ApiResponse<RestaurantDetailResponse> getRestaurant(@PathVariable Long id) {
        return ApiResponse.ok("OK", restaurantService.getRestaurantById(id));
    }

    @Operation(summary = "Get restaurant menu with optional filters")
    @GetMapping("/restaurants/{id}/menu")
    public ApiResponse<SliceResponse<ProductResponse>> getMenu(
            @PathVariable Long id,
            @Parameter(description = "Filter by category") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Search by name") @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") Long cursor,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok("OK",
                restaurantService.getMenuByRestaurantId(id, categoryId, search, cursor, size));
    }

    @Operation(summary = "Check coupon by ID or code with subtotal to calculate discount")
    @GetMapping("/coupons/check")
    public ApiResponse<CouponCheckResponse> checkCoupon(
            @Parameter(description = "Coupon ID") @RequestParam(required = false) Long couponId,
            @Parameter(description = "Coupon code") @RequestParam(required = false) String code,
            @Parameter(description = "Order subtotal to calculate discount") @RequestParam BigDecimal subtotal) {
        return ApiResponse.ok("OK", couponService.checkCoupon(couponId, code, subtotal));
    }

    @Operation(summary = "Get active coupons for a restaurant")
    @GetMapping("/restaurants/{id}/coupons")
    public ApiResponse<SliceResponse<CouponResponse>> getRestaurantCoupons(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") Long cursor,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok("OK", couponService.getActiveCouponsForRestaurant(id, cursor, size));
    }

    @Operation(summary = "Get restaurant ratings")
    @GetMapping("/restaurants/{id}/ratings")
    public ApiResponse<SliceResponse<RatingResponse>> getRestaurantRatings(
            @PathVariable Long id,
            @Parameter(description = "Filter by star (1–5)") @RequestParam(required = false) Integer star,
            @RequestParam(defaultValue = "9223372036854775807") Long cursor,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok("OK", ratingService.getRestaurantRatings(id, star, cursor, size));
    }

    @Operation(summary = "Create or update my restaurant rating")
    @PostMapping("/orders/{id}/rating")
    public ApiResponse<RatingResponse> rateRestaurant(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateRatingRequest request) {
        return ApiResponse.ok("Rating OK", ratingService.rateRestaurant(id, user, request));
    }

    @Operation(summary = "List all variants of a product")
    @GetMapping("/products/{id}/variants")
    public ApiResponse<List<VariantResponse>> getVariants(
            @PathVariable Long id) {
        return ApiResponse.ok("OK", productVariantService.findAllByProductionId(id));
    }

    @Operation(summary = "Create Stripe SetupIntent — use returned clientSecret with Stripe.js to save a card")
    @PostMapping("/cards/setup")
    public ApiResponse<SetupIntentResponse> createSetupIntent(@AuthenticationPrincipal User user) {
        return ApiResponse.ok("OK", stripeCardService.createSetupIntent(user));
    }

    @Operation(summary = "List saved cards")
    @GetMapping("/cards")
    public ApiResponse<List<CardResponse>> listCards(@AuthenticationPrincipal User user) {
        return ApiResponse.ok("OK", stripeCardService.listCards(user));
    }

    @Operation(summary = "Set a card as default")
    @PutMapping("/cards/{id}/default")
    public ApiResponse<Void> setDefaultCard(
            @AuthenticationPrincipal User user,
            @PathVariable String id) {
        stripeCardService.setDefaultCard(user, id);
        return ApiResponse.ok("Default card has been updated", null);
    }

    @Operation(summary = "Remove a saved card")
    @DeleteMapping("/cards/{id}")
    public ApiResponse<Void> removeCard(
            @AuthenticationPrincipal User user,
            @PathVariable String id) {
        stripeCardService.removeCard(user, id);
        return ApiResponse.ok("Card has been deleted", null);
    }

    @Operation(summary = "Get current user's cart items")
    @GetMapping("/cart")
    public ApiResponse<CartResponse> getCart(@AuthenticationPrincipal User user) {
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
        CartItemResponse response = cartService.updateCartItem(cartItemId, request.action());
        return ApiResponse.ok("OK", response);
    }

    @Operation(summary = "Remove item from cart")
    @DeleteMapping("/cart/items/{cartItemId}")
    public ApiResponse<Void> removeCartItem(
            @PathVariable Long cartItemId) {
        cartService.deleteCartItem(cartItemId);
        return ApiResponse.ok("OK", null);
    }

    @Operation(summary = "Create order from cart")
    @PostMapping("/orders")
    public ApiResponse<OrderResponse> createOrder(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateOrderRequest request) {
        return ApiResponse.ok("Order placed OK", orderService.createOrder(user, request));
    }

    @Operation(summary = "Get my orders")
    @GetMapping("/orders")
    public ApiResponse<SliceResponse<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "9223372036854775807") Long cursor,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        return ApiResponse.ok("OK", orderService.getMyOrders(user, cursor, size, status));
    }

    @Operation(summary = "Get order detail")
    @GetMapping("/orders/{id}")
    public ApiResponse<OrderDetailResponse> getOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ApiResponse.ok("OK", orderService.getOrderById(id, user));
    }

    @Operation(summary = "Reorder — copy all items from a previous order into current cart")
    @PostMapping("/orders/{id}/reorder")
    public ApiResponse<List<CartItemResponse>> reorder(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ApiResponse.ok("Added to cart OK", orderService.reorderFromOrder(user, id));
    }

    @Operation(summary = "Calculate shipping fee")
    @GetMapping("/shipping/fee")
    public ApiResponse<ShippingFeeResponse> calculateFee(
            @RequestParam double restaurantLat,
            @RequestParam double restaurantLong,
            @RequestParam double userLat,
            @RequestParam double userLong) {
        return ApiResponse.ok("OK",
                shippingService.calculateShippingFee(restaurantLong, restaurantLat, userLong, userLat));
    }
}
