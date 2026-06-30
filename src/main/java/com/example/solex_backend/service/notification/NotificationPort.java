package com.example.solex_backend.service.notification;

public interface NotificationPort {
    void notifyNewOrderToRestaurant(String fcmToken, Long orderId, String orderCode);
    void notifyOrderStatusToCustomer(String fcmToken, String orderCode, String status);
}
