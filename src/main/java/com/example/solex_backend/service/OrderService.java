package com.example.solex_backend.service;

import com.example.solex_backend.domain.*;
import com.example.solex_backend.dto.request.CreateOrderRequest;
import com.example.solex_backend.dto.response.OrderItemResponse;
import com.example.solex_backend.dto.response.OrderResponse;
import com.example.solex_backend.exception.BusinessException;
import com.example.solex_backend.exception.ResourceNotFoundException;
import com.example.solex_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final ShippingService shippingService;
    private final CouponService couponService;

    public OrderResponse createOrder(User user, CreateOrderRequest request) {
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        List<CartItem> cartItems = cartItemRepository.findByCart(cart);
        if (cartItems.isEmpty()) {
            throw new BusinessException("Cart is empty");
        }

        Address address = addressRepository.findById(request.addressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found: " + request.addressId()));
        if (!address.getUser().getId().equals(user.getId())) {
            throw new BusinessException("You are not allowed to use this address");
        }

        BigDecimal subtotal = cartItems.stream()
                .map(item -> item.getVariant().getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Restaurant restaurant = cartItems.get(0).getVariant().getProduct().getRestaurant();
        double shippingFeeRaw = shippingService.calculateShippingFee(
                restaurant.getLatitude(), restaurant.getLongitude(),
                address.getLatitude(), address.getLongitude()
        ).free();
        BigDecimal shippingFee = BigDecimal.valueOf(shippingFeeRaw);
        BigDecimal totalAmount = subtotal.add(shippingFee);

        Order order = Order.builder()
                .user(user)
                .address(address)
                .orderCode(generateOrderCode())
                .status("PENDING")
                .subtotal(subtotal)
                .shippingFee(shippingFee)
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(totalAmount)
                .note(request.note())
                .build();
        orderRepository.save(order);

        for (CartItem cartItem : cartItems) {
                    BigDecimal unitPrice = cartItem.getVariant().getPrice();
                    int qty = cartItem.getQuantity();
                    OrderItem orderItem = OrderItem.builder()
                            .order(order)
                            .productName(cartItem.getVariant().getProduct().getName())
                            .variant(cartItem.getVariant())
                            .sku(cartItem.getVariant().getSku())
                            .quantity(qty)
                            .unitPrice(unitPrice)
                            .subtotal(unitPrice.multiply(BigDecimal.valueOf(qty)))
                            .build();
            order.getItems().add(orderItem);
        }

        cartItemRepository.deleteAll(cartItems);

        if (request.couponId() != null) {
            couponService.applyToOrder(request.couponId(), order);
        }

        return toOrderResponse(order);
    }

    public List<OrderResponse> getMyOrders(User user) {
        return orderRepository.findByUser(user).stream()
                .map(this::toOrderResponse)
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderById(Long id, User user) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        if (!order.getUser().getId().equals(user.getId())) {
            throw new BusinessException("You are not allowed to view this order");
        }
        return toOrderResponse(order);
    }

    public OrderResponse getOrderForOperator(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        return toOrderResponse(order);
    }

    private String generateOrderCode() {
        return "ORD" + System.currentTimeMillis();
    }

    private OrderResponse toOrderResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> {
                    String productName = item.getProductName();
                    String sku = item.getSku();
                    return new OrderItemResponse(
                            item.getId(),
                            productName,
                            sku,
                            item.getQuantity(),
                            item.getUnitPrice(),
                            item.getUnitPrice().multiply(new BigDecimal(item.getQuantity()))
                    );
                })
                .collect(Collectors.toList());

        return new OrderResponse(
                order.getId(),
                order.getOrderCode(),
                order.getStatus(),
                order.getSubtotal(),
                order.getShippingFee(),
                order.getDiscountAmount(),
                order.getTotalAmount(),
                order.getNote(),
                order.getCreatedAt(),
                items
        );
    }
}
