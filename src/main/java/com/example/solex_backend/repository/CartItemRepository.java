package com.example.solex_backend.repository;

import com.example.solex_backend.domain.Cart;
import com.example.solex_backend.domain.CartItem;
import com.example.solex_backend.domain.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCart(Cart cart);
    Optional<CartItem> findByCartAndVariant(Cart cart, ProductVariant variant);

    @Modifying
    @Query(value = """
        INSERT INTO cart_items (cart_id, variant_id, quantity, unit_price)
        VALUES (:cartId, :variantId, :quantity, :unitPrice)
        ON CONFLICT (cart_id, variant_id)
        DO UPDATE SET quantity = cart_items.quantity + :quantity
        """, nativeQuery = true)
    int upsertQuantity(
            @Param("cartId") Long cartId,
            @Param("variantId") Long variantId,
            @Param("quantity") int quantity,
            @Param("unitPrice") BigDecimal unitPrice);

    @Modifying
    @Query(value = """
        UPDATE cart_items SET quantity = quantity + :delta
        WHERE id = :itemId
        """, nativeQuery = true)
    int upsertQuantityByItemId(@Param("itemId") Long itemId, @Param("delta") int delta);
}
