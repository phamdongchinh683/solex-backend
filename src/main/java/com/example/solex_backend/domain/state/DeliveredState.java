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
        throw new BusinessException("Không thể huỷ đơn hàng đã giao");
    }

    @Override
    public OrderState nextStep() {
        throw new BusinessException("Không thể chuyển tiếp từ trạng thái ĐÃ GIAO");
    }
}