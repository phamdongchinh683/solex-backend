package com.example.solex_backend.service;

import com.example.solex_backend.domain.Cart;
import com.example.solex_backend.domain.CartItem;
import com.example.solex_backend.domain.Product;
import com.example.solex_backend.domain.ProductVariant;
import com.example.solex_backend.domain.User;
import com.example.solex_backend.dto.request.AddToCartRequest;
import com.example.solex_backend.dto.response.CartItemResponse;
import com.example.solex_backend.dto.response.CartResponse;
import com.example.solex_backend.dto.response.ProductCartItemResponse;
import com.example.solex_backend.dto.response.ProductResponse;
import com.example.solex_backend.dto.response.ProductVariantResponse;
import com.example.solex_backend.exception.BusinessException;
import com.example.solex_backend.exception.ResourceNotFoundException;
import com.example.solex_backend.repository.ProductVariantRepository;
import com.example.solex_backend.repository.CartRepository;
import com.example.solex_backend.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;

    public CartResponse getCartItems(User user) {
        Cart cart = getOrCreateCart(user);
        List<CartItem> cartItems = cartItemRepository.findByCart(cart);
        List<CartItemResponse> items = cartItems.stream()
                .map(this::toCartItemResponse)
                .collect(Collectors.toList());
        if (cartItems.isEmpty()) {
            return new CartResponse(null, null, null, items);
        }
        var restaurant = cartItems.get(0).getVariant().getProduct().getRestaurant();
        return new CartResponse(restaurant.getId(), restaurant.getLatitude(), restaurant.getLongitude(), items);
    }

    public CartItemResponse addToCart(User user, AddToCartRequest request) {
        Cart cart = getOrCreateCart(user);
        ProductVariant variant = productVariantRepository.findById(request.productVariantId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product variant not found: " + request.productVariantId()));

        List<CartItem> existingItems = cartItemRepository.findByCart(cart);
        if (!existingItems.isEmpty()) {
            Long existingRestaurantId = existingItems.get(0).getVariant().getProduct().getRestaurant().getId();
            Long incomingRestaurantId = variant.getProduct().getRestaurant().getId();
            if (!existingRestaurantId.equals(incomingRestaurantId)) {
                String existingRestaurantName = existingItems.get(0).getVariant().getProduct().getRestaurant()
                        .getName();
                throw new BusinessException(
                        "Your cart already has items from restaurant \"" + existingRestaurantName + "\". " +
                                "Please clear your cart before adding items from another restaurant.");
            }
        }

        cartItemRepository.upsertQuantity(cart.getId(), variant.getId(), request.quantity(), variant.getPrice());

        CartItem item = cartItemRepository.findByCartAndVariant(cart, variant)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found after upsert"));
        return toCartItemResponse(item);
    }

    public CartItemResponse updateCartItem(Long cartItemId, String action) {

        int delta = action.equals("+") ? 1 : -1;

        CartItem item = cartItemRepository.adjustQuantity(cartItemId, delta);

        if (item == null) {
            cartItemRepository.deleteIfExhausted(cartItemId, delta);
            return null;
        }

        return toCartItemResponse(item);
    }

    public void deleteCartItem(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(user);
                    return cartRepository.save(cart);
                });
    }

    private CartItemResponse toCartItemResponse(CartItem item) {
        ProductVariant variant = item.getVariant();
        ProductCartItemResponse product = new ProductCartItemResponse(
                variant.getProduct().getId(),
                variant.getProduct().getName(),
                variant.getProduct().getDescription(),
                variant.getProduct().getImage(),
                new ProductVariantResponse(
                        variant.getId(),
                        variant.getSku(),
                        variant.getPrice(),
                        variant.getImage(),
                        variant.getSize(),
                        variant.getName(),
                        variant.getIsActive()));

        return new CartItemResponse(
                item.getId(),
                item.getQuantity(),
                product);
    }
}
