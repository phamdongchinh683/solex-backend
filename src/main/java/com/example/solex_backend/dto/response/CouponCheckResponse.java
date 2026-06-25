package com.example.solex_backend.dto.response;

import com.example.solex_backend.util.Enums.CouponDiscountType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record CouponCheckResponse(
        @Schema(description = "Coupon ID") Long id,
        @Schema(description = "Coupon code") String code,
        @Schema(description = "Restaurant ID") Long restaurantId,
        @Schema(description = "Discount type: PERCENTAGE or FIXED_AMOUNT") CouponDiscountType discountType,
        @Schema(description = "Discount value (percentage or fixed amount)") Long discountValue,
        @Schema(description = "Minimum order amount required") Long minOrderAmount,
        @Schema(description = "Maximum discount cap") Long maxDiscountAmount,
        @Schema(description = "Original subtotal") Long subtotal,
        @Schema(description = "Calculated discount amount") Long discountAmount,
        @Schema(description = "Final amount after discount") Long finalAmount,
        @Schema(description = "Valid from") LocalDateTime startDate,
        @Schema(description = "Expires at") LocalDateTime expiryDate,
        @Schema(description = "Whether coupon is currently valid") Boolean isValid,
        @Schema(description = "Validation message") String message
) {}