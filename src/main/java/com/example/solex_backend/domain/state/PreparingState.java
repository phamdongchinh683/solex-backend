package com.example.solex_backend.domain.state;

import com.example.solex_backend.exception.BusinessException;

public final class PreparingState implements OrderState {
    public static final PreparingState INSTANCE = new PreparingState();

    private PreparingState() {}

    @Override
    public String status() {
        return "PREPARING";
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
        return ReadyState.INSTANCE;
    }
}