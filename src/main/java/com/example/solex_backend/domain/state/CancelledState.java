package com.example.solex_backend.domain.state;

import com.example.solex_backend.exception.BusinessException;

public final class CancelledState implements OrderState {
    public static final CancelledState INSTANCE = new CancelledState();

    private CancelledState() {}

    @Override
    public String status() {
        return "CANCELLED";
    }

    @Override
    public OrderState confirm() {
        throw new BusinessException("Không thể xác nhận đơn hàng đã huỷ");
    }

    @Override
    public OrderState cancel() {
        return this;
    }

    @Override
    public OrderState nextStep() {
        throw new BusinessException("Không thể chuyển tiếp từ trạng thái ĐÃ HUỶ");
    }
}