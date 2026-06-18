package com.example.solex_backend.domain.state;

import com.example.solex_backend.exception.BusinessException;

public final class ReadyState implements OrderState {
    public static final ReadyState INSTANCE = new ReadyState();

    private ReadyState() {}

    @Override
    public String status() {
        return "READY";
    }

    @Override
    public OrderState confirm() {
        return this;
    }

    @Override
    public OrderState cancel() {
        return CancelledState.INSTANCE;
    }

    @Override
    public OrderState nextStep() {
        return DeliveringState.INSTANCE;
    }
}