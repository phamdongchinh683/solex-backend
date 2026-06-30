package com.example.solex_backend.service.notification;

import com.example.solex_backend.config.RabbitMQConfig;
import com.example.solex_backend.event.NewOrderNotificationEvent;
import com.example.solex_backend.event.OrderStatusNotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitMQNotificationPublisher implements NotificationPort {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void notifyNewOrderToRestaurant(String fcmToken, Long orderId, String orderCode) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.NEW_ORDER_KEY,
                new NewOrderNotificationEvent(fcmToken, orderId, orderCode));
    }

    @Override
    public void notifyOrderStatusToCustomer(String fcmToken, String orderCode, String status) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ORDER_STATUS_KEY,
                new OrderStatusNotificationEvent(fcmToken, orderCode, status));
    }
}
