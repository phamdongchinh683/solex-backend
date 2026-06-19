package com.example.solex_backend.dto.request;

import com.example.solex_backend.util.Enums.CouponDiscountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateCouponRequest(
        @Schema(description = "Unique coupon code")
        @NotBlank(message = "Code is required")
        String code,

        @Schema(description = "Discount type: PERCENTAGE or FIXED_AMOUNT")
        @NotNull(message = "Discount type is required")
        CouponDiscountType discountType,

        @Schema(description = "Discount value (e.g. 10 for 10% or 10000 for fixed)")
        @NotNull(message = "Discount value is required")
        @DecimalMin(value = "0.01", message = "Discount value must be greater than 0")
        BigDecimal discountValue,

        @Schema(description = "Minimum order subtotal required (optional)")
        BigDecimal minOrderAmount,

        @Schema(description = "Maximum discount cap for PERCENTAGE type (optional)")
        BigDecimal maxDiscountAmount,

        @Schema(description = "Maximum number of uses (null = unlimited)")
        Integer usageLimit,

        @Schema(description = "Coupon valid from")
        @NotNull(message = "Start date is required")
        LocalDateTime startDate,

        @Schema(description = "Coupon expires at")
        @NotNull(message = "Expiry date is required")
        LocalDateTime expiryDate
) {}
