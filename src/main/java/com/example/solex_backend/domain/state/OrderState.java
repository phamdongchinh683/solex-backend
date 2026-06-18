package com.example.solex_backend.domain.state;

public sealed interface OrderState
    permits PendingState, ConfirmedState, PreparingState,
            ReadyState, DeliveringState, DeliveredState, CancelledState {
    String status();
    OrderState confirm();
    OrderState cancel();
    OrderState nextStep();
}