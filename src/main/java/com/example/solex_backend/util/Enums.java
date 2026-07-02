package com.example.solex_backend.util;

public final class Enums {

    private Enums() {
    }

    public enum OtpType {
        EMAIL,
        PHONE
    }

    public enum UserRole {
        CUSTOMER,
        OPERATOR
    }

    public enum PaymentMethod {
        STRIPE, COD
    }

    public enum PaymentStatus {
        PENDING, SUCCESS, FAILED, REFUNDED
    }

    public enum CouponDiscountType {
        PERCENTAGE, FIXED_AMOUNT
    }

    public enum DeviceOs {
        IOS, ANDROID, WEB
    }

    public enum NotificationType {
        NEW_ORDER,
        ORDER_CONFIRMED,
        ORDER_PREPARING,
        ORDER_READY,
        ORDER_DELIVERING,
        ORDER_DELIVERED,
        ORDER_CANCELLED,
        PAYMENT_SUCCESS,
        PAYMENT_FAILED
    }

}
