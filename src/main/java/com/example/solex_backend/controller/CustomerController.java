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
    private final ProductService productService;
    private final ProductVariantService productVariantService;
    private final RestaurantService restaurantService;
    private final CouponService couponService;
    private final StripeCardService stripeCardService;

    @Operation(summary = "Register a new customer account")
    @PreAuthorize("permitAll()")
    @PostMapping("/sign-up")
    public ApiResponse<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.ok("Thành công", authService.signup(request));
    }

    @Operation(summary = "Get current user profile")
    @GetMapping("/profile")
    public ApiResponse<UserInfoResponse> getProfile(@AuthenticationPrincipal User user) {
        return ApiResponse.ok("Thành công", customerService.getProfile(user));
    }

    @Operation(summary = "Update user profile")
    @PutMapping("/profile")
    public ApiResponse<UserInfoResponse> updateProfile(
            @AuthenticationPrincipal User user,
            @RequestBody UpdateProfileRequest request) {
        return ApiResponse.ok("Cập nhật hồ sơ thành công", customerService.updateProfile(user, request));
    }

    @Operation(summary = "Get my addresses")
    @GetMapping("/addresses")
    public ApiResponse<List<AddressResponse>> getMyAddresses(@AuthenticationPrincipal User user) {
        return ApiResponse.ok("Thành công", addressService.getMyAddresses(user));
    }

    @Operation(summary = "Create new address")
    @PostMapping("/addresses")
    public ApiResponse<AddressResponse> createAddress(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateAddressRequest request) {
        return ApiResponse.ok("Địa chỉ đã được tạo", addressService.createAddress(user, request));
    }

    @Operation(summary = "Update address")
    @PutMapping("/addresses/{id}")
    public ApiResponse<AddressResponse> updateAddress(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody CreateAddressRequest request) {
        return ApiResponse.ok("Địa chỉ đã được cập nhật", addressService.updateAddress(id, user, request));
    }

    @Operation(summary = "Delete address")
    @DeleteMapping("/addresses/{id}")
    public ApiResponse<Void> deleteAddress(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        addressService.deleteAddress(id, user);
        return ApiResponse.ok("Địa chỉ đã được xoá", null);
    }

    @Operation(summary = "Set default address")
    @PutMapping("/addresses/{id}/default")
    public ApiResponse<AddressResponse> setDefaultAddress(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ApiResponse.ok("Địa chỉ mặc định đã được thiết lập", addressService.setDefaultAddress(id, user));
    }

    @Operation(summary = "List open restaurants")
    @GetMapping("/restaurants")
    public ApiResponse<SliceResponse<RestaurantResponse>> getAllRestaurants(
            @Parameter(description = "Search by restaurant name") @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") Long cursor,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok("Thành công", restaurantService.getAllRestaurants(name, cursor, size));
    }

    @Operation(summary = "Get restaurant detail by ID")
    @GetMapping("/restaurants/{id}")
    public ApiResponse<RestaurantDetailResponse> getRestaurant(@PathVariable Long id) {
        return ApiResponse.ok("Thành công", restaurantService.getRestaurantById(id));
    }

    @Operation(summary = "Get restaurant menu with optional filters")
    @GetMapping("/restaurants/{restaurantId}/menu")
    public ApiResponse<SliceResponse<ProductResponse>> getMenu(
            @PathVariable Long restaurantId,
            @Parameter(description = "Filter by category") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Search by name") @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") Long cursor,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok("Thành công",
                restaurantService.getMenuByRestaurantId(restaurantId, categoryId, search, cursor, size));
    }

    @Operation(summary = "Check coupon by ID or code with subtotal to calculate discount")
    @GetMapping("/coupons/check")
    public ApiResponse<CouponCheckResponse> checkCoupon(
            @Parameter(description = "Coupon ID") @RequestParam(required = false) Long couponId,
            @Parameter(description = "Coupon code") @RequestParam(required = false) String code,
            @Parameter(description = "Order subtotal to calculate discount") @RequestParam BigDecimal subtotal) {
        return ApiResponse.ok("Thành công", couponService.checkCoupon(couponId, code, subtotal));
    }

    @Operation(summary = "Get active coupons for a restaurant")
    @GetMapping("/restaurants/{restaurantId}/coupons")
    public ApiResponse<SliceResponse<CouponResponse>> getRestaurantCoupons(
            @PathVariable Long restaurantId,
            @RequestParam(defaultValue = "0") Long cursor,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok("Thành công", couponService.getActiveCouponsForRestaurant(restaurantId, cursor, size));
    }

    @Operation(summary = "Get restaurant ratings")
    @GetMapping("/restaurants/{restaurantId}/ratings")
    public ApiResponse<SliceResponse<RatingResponse>> getRestaurantRatings(
            @PathVariable Long restaurantId,
            @Parameter(description = "Filter by star (1–5)") @RequestParam(required = false) Integer star,
            @RequestParam(defaultValue = "9223372036854775807") Long cursor,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok("Thành công", ratingService.getRestaurantRatings(restaurantId, star, cursor, size));
    }

    @Operation(summary = "Create or update my restaurant rating")
    @PostMapping("/restaurants/{restaurantId}/ratings")
    public ApiResponse<RatingResponse> rateRestaurant(
            @PathVariable Long restaurantId,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateRatingRequest request) {
        return ApiResponse.ok("Đánh giá thành công", ratingService.rateRestaurant(restaurantId, user, request));
    }

    @Operation(summary = "List all variants of a product")
    @GetMapping("/products/{id}/variants")
    public ApiResponse<List<VariantResponse>> getVariants(
            @PathVariable Long id) {
        return ApiResponse.ok("Thành công", productVariantService.findAllByProductionId(id));
    }

    @Operation(summary = "Create Stripe SetupIntent — use returned clientSecret with Stripe.js to save a card")
    @PostMapping("/cards/setup")
    public ApiResponse<SetupIntentResponse> createSetupIntent(@AuthenticationPrincipal User user) {
        return ApiResponse.ok("Thành công", stripeCardService.createSetupIntent(user));
    }

    @Operation(summary = "List saved cards")
    @GetMapping("/cards")
    public ApiResponse<List<CardResponse>> listCards(@AuthenticationPrincipal User user) {
        return ApiResponse.ok("Thành công", stripeCardService.listCards(user));
    }

    @Operation(summary = "Set a card as default")
    @PutMapping("/cards/{paymentMethodId}/default")
    public ApiResponse<Void> setDefaultCard(
            @AuthenticationPrincipal User user,
            @PathVariable String paymentMethodId) {
        stripeCardService.setDefaultCard(user, paymentMethodId);
        return ApiResponse.ok("Thẻ mặc định đã được cập nhật", null);
    }

    @Operation(summary = "Remove a saved card")
    @DeleteMapping("/cards/{paymentMethodId}")
    public ApiResponse<Void> removeCard(
            @AuthenticationPrincipal User user,
            @PathVariable String paymentMethodId) {
        stripeCardService.removeCard(user, paymentMethodId);
        return ApiResponse.ok("Thẻ đã được xoá", null);
    }

    @Operation(summary = "Get current user's cart items")
    @GetMapping("/cart")
    public ApiResponse<List<CartItemResponse>> getCart(@AuthenticationPrincipal User user) {
        return ApiResponse.ok("Thành công", cartService.getCartItems(user));
    }

    @Operation(summary = "Add item to cart")
    @PostMapping("/cart/items")
    public ApiResponse<CartItemResponse> addToCart(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AddToCartRequest request) {
        return ApiResponse.ok("Thành công", cartService.addToCart(user, request));
    }

    @Operation(summary = "Update cart item quantity (+/-)")
    @PostMapping("/cart/items/{cartItemId}")
    public ApiResponse<CartItemResponse> updateCartItem(
            @AuthenticationPrincipal User user,
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        CartItemResponse response = cartService.updateCartItem(user, cartItemId, request.action());
        return response != null
                ? ApiResponse.ok("Thành công", response)
                : ApiResponse.ok("Đã xoá khỏi giỏ hàng", null);
    }

    @Operation(summary = "Remove item from cart")
    @DeleteMapping("/cart/items/{cartItemId}")
    public ApiResponse<Void> removeCartItem(
            @AuthenticationPrincipal User user,
            @PathVariable Long cartItemId) {
        cartService.deleteCartItem(user, cartItemId);
        return ApiResponse.ok("Thành công", null);
    }

    @Operation(summary = "Create order from cart")
    @PostMapping("/orders")
    public ApiResponse<OrderResponse> createOrder(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateOrderRequest request) {
        return ApiResponse.ok("Đặt hàng thành công", orderService.createOrder(user, request));
    }

    @Operation(summary = "Get my orders")
    @GetMapping("/orders")
    public ApiResponse<SliceResponse<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "9223372036854775807") Long cursor,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok("Thành công", orderService.getMyOrders(user, cursor, size));
    }

    @Operation(summary = "Get order detail")
    @GetMapping("/orders/{orderId}")
    public ApiResponse<OrderResponse> getOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal User user) {
        return ApiResponse.ok("Thành công", orderService.getOrderById(orderId, user));
    }

    @Operation(summary = "Calculate shipping fee")
    @GetMapping("/shipping/fee")
    public ApiResponse<ShippingFeeResponse> calculateFee(
            @RequestParam double restaurantLat,
            @RequestParam double restaurantLng,
            @RequestParam double userLat,
            @RequestParam double userLng) {
        return ApiResponse.ok("Thành công",
                shippingService.calculateShippingFee(restaurantLng, restaurantLat, userLng, userLat));
    }
}
