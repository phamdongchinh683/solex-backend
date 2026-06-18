package com.example.solex_backend.domain.state;

import com.example.solex_backend.exception.BusinessException;

public final class PendingState implements OrderState {
    public static final PendingState INSTANCE = new PendingState();

    private PendingState() {}

    @Override
    public String status() {
        return "PENDING";
    }

    @Override
    public OrderState confirm() {
        return ConfirmedState.INSTANCE;
    }

    @Override
    public OrderState cancel() {
        return CancelledState.INSTANCE;
    }

    @Override
    public OrderState nextStep() {
        throw new BusinessException("Cannot advance from PENDING state");
    }
}