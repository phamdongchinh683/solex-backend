package com.example.solex_backend.domain.state;


public final class ConfirmedState implements OrderState {
    public static final ConfirmedState INSTANCE = new ConfirmedState();

    private ConfirmedState() {}

    @Override
    public String status() {
        return "CONFIRMED";
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
        return PreparingState.INSTANCE;
    }
}