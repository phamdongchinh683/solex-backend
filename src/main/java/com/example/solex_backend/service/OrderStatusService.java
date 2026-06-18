package com.example.solex_backend.service;

import com.example.solex_backend.domain.Order;
import com.example.solex_backend.domain.OrderStatusHistory;
import com.example.solex_backend.domain.User;
import com.example.solex_backend.domain.state.OrderState;
import com.example.solex_backend.domain.state.OrderStateFactory;
import com.example.solex_backend.exception.BusinessException;
import com.example.solex_backend.exception.ResourceNotFoundException;
import com.example.solex_backend.repository.OrderRepository;
import com.example.solex_backend.repository.OrderStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderStatusService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    public void confirmOrder(Long orderId, User operator) {
        Order order = getOrderOrThrow(orderId);
        OrderState newState = OrderStateFactory.fromString(order.getStatus()).confirm();
        updateStatus(order, newState, operator, "Order confirmed");
    }

    public void advanceOrder(Long orderId, User operator) {
        Order order = getOrderOrThrow(orderId);
        OrderState newState = OrderStateFactory.fromString(order.getStatus()).nextStep();
        updateStatus(order, newState, operator, "Order advanced to next step");
    }

    public void cancelOrder(Long orderId, User operator, String reason) {
        Order order = getOrderOrThrow(orderId);
        OrderState newState = OrderStateFactory.fromString(order.getStatus()).cancel();
        updateStatus(order, newState, operator, reason);
    }

    public void confirmOrderByPayment(Long orderId) {
        Order order = getOrderOrThrow(orderId);
        OrderState newState = OrderStateFactory.fromString(order.getStatus()).confirm();
        updateStatus(order, newState, null, "Payment confirmed automatically");
    }

    public void cancelOrderByPayment(Long orderId) {
        Order order = getOrderOrThrow(orderId);
        OrderState newState = OrderStateFactory.fromString(order.getStatus()).cancel();
        updateStatus(order, newState, null, "Payment failed - order cancelled automatically");
    }

    private Order getOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    }

    private void updateStatus(Order order, OrderState newState, User changedBy, String note) {
        String oldStatus = order.getStatus();
        String newStatus = newState.status();

        if (oldStatus.equals(newStatus)) {
            return;
        }

        order.setStatus(newStatus);
        orderRepository.save(order);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .status(newStatus)
                .note(note)
                .changedBy(changedBy)
                .build();
        orderStatusHistoryRepository.save(history);
    }
}