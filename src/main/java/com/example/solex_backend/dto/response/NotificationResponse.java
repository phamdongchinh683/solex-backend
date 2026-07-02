package com.example.solex_backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record NotificationResponse(
        @Schema(description = "Notification ID") Long id,
        @Schema(description = "Related order ID") Long orderId,
        @Schema(description = "Related order code") String orderCode,
        @Schema(description = "Notification type") String type,
        @Schema(description = "Title") String title,
        @Schema(description = "Body message") String body,
        @Schema(description = "Whether user has read it") Boolean isRead,
        @Schema(description = "Created timestamp") LocalDateTime createdAt) {
}
