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
        STRIPE, VNPAY, COD
    }

    public enum PaymentStatus {
        PENDING, SUCCESS, FAILED, REFUNDED
    }

    public enum CouponDiscountType {
        PERCENTAGE, FIXED_AMOUNT
    }

}