package com.neurocart.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDTO {

    @Data
    public static class PlaceOrderRequest {
        private String shippingAddress;
        private String paymentMethod;
        private String couponCode;
        private String notes;
    }

    @Data
    @Builder
    public static class OrderResponse {
        private Long id;
        private String orderNumber;
        private String status;
        private List<OrderItemResponse> orderItems;
        private BigDecimal subtotal;
        private BigDecimal discountAmount;
        private BigDecimal shippingCharge;
        private BigDecimal totalAmount;
        private String couponCode;
        private String shippingAddress;
        private String paymentMethod;
        private String paymentStatus;
        private String trackingNumber;
        private LocalDateTime createdAt;
        private LocalDateTime packedAt;
        private LocalDateTime shippedAt;
        private LocalDateTime deliveredAt;
    }

    @Data
    @Builder
    public static class OrderItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private String productImage;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
    }
}
