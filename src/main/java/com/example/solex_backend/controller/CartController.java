package com.example.solex_backend.controller;

import com.example.solex_backend.domain.User;
import com.example.solex_backend.dto.ApiResponse;
import com.example.solex_backend.dto.request.AddToCartRequest;
import com.example.solex_backend.dto.request.UpdateCartItemRequest;
import com.example.solex_backend.dto.response.CartItemResponse;
import com.example.solex_backend.service.CartService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Tag(name = "Cart", description = "Shopping cart management")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/cart")
@PreAuthorize("hasRole('CUSTOMER')")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @Operation(summary = "Get current user's cart items")
    @GetMapping
    public ApiResponse<List<CartItemResponse>> getCart(@AuthenticationPrincipal User user) {
        return ApiResponse.ok("OK", cartService.getCartItems(user));
    }

    @Operation(summary = "Add item to cart")
    @PostMapping("/items")
    public ApiResponse<CartItemResponse> addToCart(@AuthenticationPrincipal User user, @RequestBody @Valid AddToCartRequest request) {
        return ApiResponse.ok("OK", cartService.addToCart(user, request));
    }

    @Operation(summary = "Remove item from cart")
    @DeleteMapping("/items/{cartItemId}")
    public ApiResponse<Void> removeCartItem(
            @AuthenticationPrincipal User user,
            @PathVariable Long cartItemId) {
        cartService.deleteCartItem(user, cartItemId);
        return ApiResponse.ok("OK", null);
    }

    @Operation(summary = "Update cart item (+/-)")
    @PostMapping("/items/{cartItemId}")
    public ApiResponse<CartItemResponse> updateCartItem(
            @AuthenticationPrincipal User user,
            @PathVariable Long cartItemId,
            @RequestBody @Valid UpdateCartItemRequest request) {
        CartItemResponse response = cartService.updateCartItem(user, cartItemId, request.action());
        if (response == null) {
            return ApiResponse.ok("Removed", null);
        }
        return ApiResponse.ok("OK", response);
    }
}
