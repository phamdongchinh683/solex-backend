package com.example.solex_backend.event;

public record NewOrderNotificationEvent(String fcmToken, Long orderId, String orderCode) {}
