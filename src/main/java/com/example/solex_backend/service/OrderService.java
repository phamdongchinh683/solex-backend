package com.example.solex_backend.service;

import com.example.solex_backend.domain.*;
import com.example.solex_backend.dto.request.CreateOrderRequest;
import com.example.solex_backend.dto.response.CartItemResponse;
import com.example.solex_backend.dto.response.OrderDetailResponse;
import com.example.solex_backend.dto.response.OrderItemResponse;
import com.example.solex_backend.dto.response.OrderResponse;
import com.example.solex_backend.dto.response.ProductCartItemResponse;
import com.example.solex_backend.dto.response.ProductVariantResponse;
import com.example.solex_backend.dto.response.SliceResponse;
import com.example.solex_backend.exception.BusinessException;
import com.example.solex_backend.exception.ResourceNotFoundException;
import com.example.solex_backend.repository.*;
import com.example.solex_backend.service.notification.NotificationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.solex_backend.domain.state.OrderStateFactory;

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
        private final RestaurantRepository restaurantRepository;
        private final PaymentRepository paymentRepository;
        private final ShippingService shippingService;
        private final CouponService couponService;
        private final NotificationPort notificationPort;
        private final NotificationService notificationService;

        public OrderResponse createOrder(User user, CreateOrderRequest request) {
                Cart cart = cartRepository.findByUser(user)
                                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

                List<CartItem> cartItems = cartItemRepository.findByCart(cart);
                if (cartItems.isEmpty()) {
                        throw new BusinessException("Cart is empty");
                }

                Address address = addressRepository.findByIdAndUser(request.addressId(), user)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Address not found: " + request.addressId()));;
                BigDecimal subtotal = cartItems.stream()
                                .map(item -> item.getVariant().getPrice().multiply(new BigDecimal(item.getQuantity())))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                Restaurant restaurant = cartItems.get(0).getVariant().getProduct().getRestaurant();

                double shippingFeeRaw = shippingService.calculateShippingFee(
                                restaurant.getLongitude(), restaurant.getLatitude(),
                                address.getLongitude(), address.getLatitude()).fee();

                BigDecimal shippingFee = BigDecimal.valueOf(shippingFeeRaw);
                BigDecimal totalAmount = subtotal.add(shippingFee);

                Order order = Order.builder()
                                .user(user)
                                .restaurant(restaurant)
                                .address(address)
                                .orderCode(generateOrderCode())
                                .status("PENDING")
                                .subtotal(subtotal)
                                .shippingFee(shippingFee)
                                .discountAmount(BigDecimal.ZERO)
                                .totalAmount(totalAmount)
                                .note(request.note())
                                .rate(false)
                                .restaurant(restaurant)
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

                User operator = restaurant.getOperator();
                if (operator != null && operator.getFcmToken() != null) {
                        notificationPort.notifyNewOrderToRestaurant(
                                        operator.getFcmToken(), order.getId(), order.getOrderCode());
                        notificationService.createOrderNotification(
                                        operator, order, OrderStateFactory.fromString(order.getStatus()),
                                        "New Order Received", "You have a new order: " + order.getOrderCode());

                }

                return toOrderResponse(order);
        }

        public List<CartItemResponse> reorderFromOrder(User user, Long orderId) {
                Order order = orderRepository.findByIdAndUser(orderId, user)
                                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

                Cart cart = getOrCreateCart(user);

                List<CartItem> existingItems = cartItemRepository.findByCart(cart);
                if (!existingItems.isEmpty()) {
                        Long existingRestaurantId = existingItems.get(0).getVariant().getProduct().getRestaurant()
                                        .getId();
                        Long orderRestaurantId = order.getRestaurant().getId();
                        if (!existingRestaurantId.equals(orderRestaurantId)) {
                                cartItemRepository.deleteAll(existingItems);
                        }
                }

                for (OrderItem orderItem : order.getItems()) {
                        ProductVariant variant = orderItem.getVariant();
                        if (Boolean.TRUE.equals(variant.getIsActive())) {
                                cartItemRepository.upsertQuantity(
                                                cart.getId(),
                                                variant.getId(),
                                                orderItem.getQuantity(),
                                                variant.getPrice());
                        }
                }

                return cartItemRepository.findByCart(cart).stream()
                                .map(this::toCartItemResponse)
                                .collect(Collectors.toList());
        }

        public SliceResponse<OrderResponse> getMyOrders(User user, Long cursor, int size, String status) {
                List<Order> result = orderRepository.findByUserBeforeCursor(user, cursor, status,
                                PageRequest.of(0, size + 1));
                boolean hasNext = result.size() > size;
                List<Order> page = hasNext ? result.subList(0, size) : result;
                Long nextCursor = hasNext ? page.get(page.size() - 1).getId() : null;
                return new SliceResponse<>(page.stream().map(this::toOrderResponse).toList(), nextCursor);
        }

        public SliceResponse<OrderResponse> getOrdersByRestaurant(User operator, Long cursor, int size, String status) {
                Restaurant restaurant = restaurantRepository.findByOperator(operator)
                                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found for operator"));
                List<Order> result = orderRepository.findByRestaurantBeforeCursor(restaurant.getId(), cursor, status,
                                PageRequest.of(0, size + 1));
                boolean hasNext = result.size() > size;
                List<Order> page = hasNext ? result.subList(0, size) : result;
                Long nextCursor = hasNext ? page.get(page.size() - 1).getId() : null;
                return new SliceResponse<>(page.stream().map(this::toOrderResponse).toList(), nextCursor);
        }

        public OrderDetailResponse getOrderById(Long id, User user) {
                Order order = orderRepository.findByIdAndUser(id, user)
                                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
                return toOrderDetailResponse(order);
        }

        public OrderDetailResponse getOrderForOperator(Long id) {
                Order order = orderRepository.findByIdWithItems(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
                return toOrderDetailResponse(order);
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
                return new CartItemResponse(
                                item.getId(),
                                item.getQuantity(),
                                new ProductCartItemResponse(
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
                                                                variant.getIsActive())));
        }

        private String generateOrderCode() {
                return "XLORD" + System.currentTimeMillis();
        }

        private OrderResponse toOrderResponse(Order order) {
                return new OrderResponse(
                                order.getId(),
                                order.getOrderCode(),
                                order.getStatus(),
                                order.getSubtotal(),
                                order.getShippingFee(),
                                order.getDiscountAmount(),
                                order.getTotalAmount(),
                                order.getNote(),
                                order.getCreatedAt());
        }

        private OrderDetailResponse toOrderDetailResponse(Order order) {
                List<OrderItemResponse> items = order.getItems().stream()
                                .map(item -> new OrderItemResponse(
                                                item.getId(),
                                                item.getProductName(),
                                                item.getSku(),
                                                item.getQuantity(),
                                                item.getUnitPrice(),
                                                item.getUnitPrice().multiply(new BigDecimal(item.getQuantity()))))
                                .collect(Collectors.toList());

                Payment latestPayment = paymentRepository.findTopByOrderOrderByIdDesc(order).orElse(null);

                return new OrderDetailResponse(
                                order.getId(),
                                order.getOrderCode(),
                                order.getStatus(),
                                order.getSubtotal(),
                                order.getShippingFee(),
                                order.getDiscountAmount(),
                                order.getTotalAmount(),
                                order.getNote(),
                                order.getRate(),
                                order.getCreatedAt(),
                                order.getRestaurant().getLatitude(),
                                order.getRestaurant().getLongitude(),
                                order.getAddress().getLatitude(),
                                order.getAddress().getLongitude(),
                                latestPayment != null ? latestPayment.getMethod() : null,
                                latestPayment != null ? latestPayment.getStatus() : null,
                                latestPayment != null ? latestPayment.getTransactionRef() : null,
                                items);
        }
}