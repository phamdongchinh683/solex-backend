package com.example.solex_backend.event;

public record OrderStatusNotificationEvent(String fcmToken, String orderCode, String status) {}
