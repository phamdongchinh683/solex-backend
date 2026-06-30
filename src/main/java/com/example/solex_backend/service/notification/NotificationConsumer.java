package com.example.solex_backend.service.notification;

import com.example.solex_backend.config.RabbitMQConfig;
import com.example.solex_backend.event.NewOrderNotificationEvent;
import com.example.solex_backend.event.OrderStatusNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final FirebaseNotificationAdapter firebaseAdapter;

    @RabbitListener(queues = RabbitMQConfig.NEW_ORDER_QUEUE)
    public void handleNewOrder(NewOrderNotificationEvent event) {
        firebaseAdapter.notifyNewOrderToRestaurant(event.fcmToken(), event.orderId(), event.orderCode());
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_STATUS_QUEUE)
    public void handleOrderStatus(OrderStatusNotificationEvent event) {
        firebaseAdapter.notifyOrderStatusToCustomer(event.fcmToken(), event.orderCode(), event.status());
    }
}
