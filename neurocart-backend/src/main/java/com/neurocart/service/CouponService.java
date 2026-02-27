package com.neurocart.service;

import com.neurocart.entity.Coupon;
import com.neurocart.exception.BadRequestException;
import com.neurocart.exception.ResourceNotFoundException;
import com.neurocart.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    public BigDecimal calculateDiscount(String code, BigDecimal orderAmount) {
        Coupon coupon = couponRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new BadRequestException("Coupon code not found: " + code));

        if (!coupon.isActive())
            throw new BadRequestException("Coupon is inactive");
        if (coupon.getExpiresAt() != null && coupon.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Coupon has expired");
        }
        if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new BadRequestException("Coupon usage limit reached");
        }
        if (orderAmount.compareTo(coupon.getMinOrderAmount()) < 0) {
            throw new BadRequestException("Minimum order amount for this coupon is ₹" + coupon.getMinOrderAmount());
        }

        BigDecimal discount;
        if (coupon.getDiscountType() == Coupon.DiscountType.PERCENTAGE) {
            discount = orderAmount.multiply(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100));
            if (coupon.getMaxDiscountAmount() != null) {
                discount = discount.min(coupon.getMaxDiscountAmount());
            }
        } else {
            discount = coupon.getDiscountValue().min(orderAmount);
        }

        return discount.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    @Transactional
    public void incrementUsage(String code) {
        couponRepository.findByCode(code.toUpperCase()).ifPresent(coupon -> {
            coupon.setUsedCount(coupon.getUsedCount() + 1);
            couponRepository.save(coupon);
        });
    }

    public Coupon getCouponByCode(String code) {
        return couponRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found: " + code));
    }

    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    @Transactional
    public Coupon createCoupon(Coupon coupon) {
        if (couponRepository.existsByCode(coupon.getCode().toUpperCase())) {
            throw new BadRequestException("Coupon code already exists");
        }
        coupon.setCode(coupon.getCode().toUpperCase());
        return couponRepository.save(coupon);
    }
}
