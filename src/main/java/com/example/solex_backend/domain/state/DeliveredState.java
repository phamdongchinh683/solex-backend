package com.example.solex_backend.domain.state;

import com.example.solex_backend.exception.BusinessException;

public final class DeliveredState implements OrderState {
    public static final DeliveredState INSTANCE = new DeliveredState();

    private DeliveredState() {}

    @Override
    public String status() {
        return "DELIVERED";
    }

    @Override
    public OrderState confirm() {
        return this;
    }

    @Override
    public OrderState cancel() {
        throw new BusinessException("Cannot cancel a delivered order");
    }

    @Override
    public OrderState nextStep() {
        throw new BusinessException("Cannot advance from DELIVERED state");
    }
}