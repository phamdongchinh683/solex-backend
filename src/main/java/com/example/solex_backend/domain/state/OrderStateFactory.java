package com.example.solex_backend.domain.state;

import com.example.solex_backend.exception.BusinessException;

public final class OrderStateFactory {
    private OrderStateFactory() {}

    public static OrderState fromString(String status) {
        return switch (status) {
            case "PENDING" -> PendingState.INSTANCE;
            case "CONFIRMED" -> ConfirmedState.INSTANCE;
            case "PREPARING" -> PreparingState.INSTANCE;
            case "READY" -> ReadyState.INSTANCE;
            case "DELIVERING" -> DeliveringState.INSTANCE;
            case "DELIVERED" -> DeliveredState.INSTANCE;
            case "CANCELLED" -> CancelledState.INSTANCE;
            default -> throw new BusinessException("Unknown order status: " + status);
        };
    }
}