package com.example.solex_backend.service.notification;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FirebaseNotificationAdapter {

    public void notifyNewOrderToRestaurant(String fcmToken, Long orderId, String orderCode) {
        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                        .setTitle("New Order!")
                        .setBody("You have a new order " + orderCode + " to confirm")
                        .build())
                .putData("type", "NEW_ORDER")
                .putData("orderId", String.valueOf(orderId))
                .putData("orderCode", orderCode)
                .build();
        send(message);
    }

    public void notifyOrderStatusToCustomer(String fcmToken, String orderCode, String status) {
        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                        .setTitle("Order Update")
                        .setBody(buildStatusMessage(orderCode, status))
                        .build())
                .putData("type", "ORDER_STATUS")
                .putData("orderCode", orderCode)
                .putData("status", status)
                .build();
        send(message);
    }

    private void send(Message message) {
        if (FirebaseApp.getApps().isEmpty()) {
            return;
        }
        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (Exception e) {
            log.warn("FCM send failed: {}", e.getMessage());
        }
    }

    private String buildStatusMessage(String orderCode, String status) {
        return switch (status) {
            case "CONFIRMED" -> "Order " + orderCode + " has been confirmed";
            case "PREPARING" -> "Order " + orderCode + " is being prepared";
            case "READY" -> "Order " + orderCode + " is ready, the store will deliver to you soon";
            case "DELIVERING" -> "Order " + orderCode + " is on its way to you";
            case "DELIVERED" -> "Order " + orderCode + " has been delivered. Thank you!";
            case "CANCELLED" -> "Order " + orderCode + " has been cancelled";
            default -> "Order " + orderCode + " status has been updated";
        };
    }
}
