package com.example.solex_backend.dto.response;

import com.example.solex_backend.util.Enums.CouponDiscountType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CouponResponse(
        @Schema(description = "Coupon ID") Long id,
        @Schema(description = "Restaurant ID") Long restaurantId,
        @Schema(description = "Coupon code") String code,
        @Schema(description = "Discount type: PERCENTAGE or FIXED_AMOUNT") CouponDiscountType discountType,
        @Schema(description = "Discount value") BigDecimal discountValue,
        @Schema(description = "Minimum order amount to apply") BigDecimal minOrderAmount,
        @Schema(description = "Maximum discount cap") BigDecimal maxDiscountAmount,
        @Schema(description = "Maximum usage limit (null = unlimited)") Integer usageLimit,
        @Schema(description = "Times this coupon has been used") Integer usageCount,
        @Schema(description = "Valid from") LocalDateTime startDate,
        @Schema(description = "Expires at") LocalDateTime expiryDate,
        @Schema(description = "Whether coupon is active") Boolean isActive,
        @Schema(description = "Created at") LocalDateTime createdAt
) {}
