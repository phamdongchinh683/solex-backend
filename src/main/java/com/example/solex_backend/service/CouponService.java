package com.example.solex_backend.service;

import com.example.solex_backend.domain.Coupon;
import com.example.solex_backend.domain.Order;
import com.example.solex_backend.exception.BusinessException;
import com.example.solex_backend.exception.ResourceNotFoundException;
import com.example.solex_backend.repository.CouponRepository;
import com.example.solex_backend.repository.OrderRepository;
import com.example.solex_backend.util.Enums.CouponDiscountType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class CouponService {

    private final CouponRepository couponRepository;
    private final OrderRepository orderRepository;

    /**
     * Validates the coupon and applies the discount to the order.
     * Updates order.discountAmount, order.totalAmount, order.coupon,
     * and increments coupon.usageCount. Saves both entities.
     */
    public void applyToOrder(Long couponId, Order order) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found: " + couponId));

        validateCoupon(coupon, order);

        BigDecimal discount = calculateDiscount(coupon, order.getSubtotal());

        order.setCoupon(coupon);
        order.setDiscountAmount(discount);
        order.setTotalAmount(
                order.getSubtotal()
                        .add(order.getShippingFee())
                        .subtract(discount)
                        .max(BigDecimal.ZERO)
        );
        orderRepository.save(order);

        coupon.setUsageCount(coupon.getUsageCount() + 1);
        couponRepository.save(coupon);
    }

    private void validateCoupon(Coupon coupon, Order order) {
        if (!Boolean.TRUE.equals(coupon.getIsActive())) {
            throw new BusinessException("Coupon is not active");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartDate()) || now.isAfter(coupon.getExpiryDate())) {
            throw new BusinessException("Coupon is expired or not yet valid");
        }

        if (coupon.getUsageLimit() != null && coupon.getUsageCount() >= coupon.getUsageLimit()) {
            throw new BusinessException("Coupon usage limit has been reached");
        }

        if (coupon.getMinOrderAmount() != null
                && order.getSubtotal().compareTo(coupon.getMinOrderAmount()) < 0) {
            throw new BusinessException(
                    "Order subtotal does not meet the minimum required amount of " + coupon.getMinOrderAmount());
        }

        if (order.getCoupon() != null) {
            throw new BusinessException("A coupon has already been applied to this order");
        }
    }

    private BigDecimal calculateDiscount(Coupon coupon, BigDecimal subtotal) {
        BigDecimal discount;

        if (coupon.getDiscountType() == CouponDiscountType.PERCENTAGE) {
            discount = subtotal
                    .multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            discount = coupon.getDiscountValue();
        }

        if (coupon.getMaxDiscountAmount() != null) {
            discount = discount.min(coupon.getMaxDiscountAmount());
        }

        return discount;
    }
}
