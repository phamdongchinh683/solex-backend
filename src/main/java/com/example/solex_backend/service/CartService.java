package com.example.solex_backend.service;

import com.example.solex_backend.domain.Cart;
import com.example.solex_backend.domain.CartItem;
import com.example.solex_backend.domain.ProductVariant;
import com.example.solex_backend.domain.User;
import com.example.solex_backend.dto.request.AddToCartRequest;
import com.example.solex_backend.dto.response.CartItemResponse;
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

    public List<CartItemResponse> getCartItems(User user) {
        Cart cart = getOrCreateCart(user);
        return cartItemRepository.findByCart(cart).stream()
                .map(this::toCartItemResponse)
                .collect(Collectors.toList());
    }

    public CartItemResponse addToCart(User user, AddToCartRequest request) {
        Cart cart = getOrCreateCart(user);
        ProductVariant variant = productVariantRepository.findById(request.productVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Product variant not found: " + request.productVariantId()));

        List<CartItem> existingItems = cartItemRepository.findByCart(cart);
        if (!existingItems.isEmpty()) {
            Long existingRestaurantId = existingItems.get(0).getVariant().getProduct().getRestaurant().getId();
            Long incomingRestaurantId = variant.getProduct().getRestaurant().getId();
            if (!existingRestaurantId.equals(incomingRestaurantId)) {
                String existingRestaurantName = existingItems.get(0).getVariant().getProduct().getRestaurant().getName();
                throw new BusinessException(
                        "Your cart already has items from \"" + existingRestaurantName + "\". " +
                        "Please clear your cart before adding items from a different restaurant."
                );
            }
        }

        cartItemRepository.upsertQuantity(cart.getId(), variant.getId(), request.quantity(), variant.getPrice());

        CartItem item = cartItemRepository.findByCartAndVariant(cart, variant)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found after upsert"));
        return toCartItemResponse(item);
    }

    public CartItemResponse updateCartItem(User user, Long cartItemId, String action) {
        if (!action.equals("+") && !action.equals("-")) {
            throw new BusinessException("Invalid action. Use '+' or '-'");
        }

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + cartItemId));

        if (!item.getCart().getUser().getId().equals(user.getId())) {
            throw new BusinessException("You are not allowed to update this cart item");
        }

        int delta = action.equals("+") ? 1 : -1;
        int nextQuantity = item.getQuantity() + delta;
        if (nextQuantity <= 0) {
            cartItemRepository.delete(item);
            return null;
        }

        item.setQuantity(nextQuantity);
        cartItemRepository.save(item);
        return toCartItemResponse(item);
    }

    public void deleteCartItem(User user, Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + cartItemId));
        if (!item.getCart().getUser().getId().equals(user.getId())) {
            throw new BusinessException("You are not allowed to delete this cart item");
        }
        cartItemRepository.delete(item);
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
        ProductResponse product = new ProductResponse(
                variant.getProduct().getId(),
                variant.getProduct().getName(),
                variant.getProduct().getDescription(),
                variant.getProduct().getBasePrice(),
                variant.getProduct().getIsActive(),
                variant.getProduct().getCategory().getId(),
                variant.getProduct().getCategory().getName(),
                List.of(),
                List.of()
        );
        ProductVariantResponse variantResponse = new ProductVariantResponse(
                variant.getId(), variant.getSku(), variant.getSize(),
                variant.getPrice(), variant.getStock(), variant.getImageUrl(), variant.getIsActive()
        );
        BigDecimal itemPrice = variant.getPrice().multiply(new BigDecimal(item.getQuantity()));
        return new CartItemResponse(item.getId(), product, variantResponse, item.getQuantity(), itemPrice);
    }
}
