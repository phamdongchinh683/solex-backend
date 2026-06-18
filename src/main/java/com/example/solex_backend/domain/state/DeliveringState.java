package com.example.solex_backend.domain.state;


public final class DeliveringState implements OrderState {
    public static final DeliveringState INSTANCE = new DeliveringState();

    private DeliveringState() {}

    @Override
    public String status() {
        return "DELIVERING";
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
        return DeliveredState.INSTANCE;
    }
}