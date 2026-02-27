package com.neurocart.service;

import com.neurocart.dto.OrderDTO;
import com.neurocart.entity.*;
import com.neurocart.exception.BadRequestException;
import com.neurocart.exception.ResourceNotFoundException;
import com.neurocart.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    private final CouponService couponService;

    @Transactional
    public OrderDTO.OrderResponse placeOrder(User user, OrderDTO.PlaceOrderRequest request) {
        List<Cart> cartItems = cartRepository.findByUserId(user.getId());
        if (cartItems.isEmpty())
            throw new BadRequestException("Cart is empty");

        // Validate stock for all items
        for (Cart item : cartItems) {
            Product p = item.getProduct();
            if (p.getStockQuantity() < item.getQuantity()) {
                throw new BadRequestException("Insufficient stock for: " + p.getName());
            }
        }

        // Calculate subtotal
        BigDecimal subtotal = cartItems.stream()
                .map(c -> c.getProduct().getCurrentPrice().multiply(BigDecimal.valueOf(c.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Apply coupon
        BigDecimal discount = BigDecimal.ZERO;
        String couponCode = request.getCouponCode();
        if (couponCode != null && !couponCode.isBlank()) {
            discount = couponService.calculateDiscount(couponCode, subtotal);
        }

        BigDecimal shipping = subtotal.compareTo(BigDecimal.valueOf(500)) >= 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(50);
        BigDecimal total = subtotal.subtract(discount).add(shipping).max(BigDecimal.ZERO);

        // Build order
        Order order = Order.builder()
                .orderNumber("NC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .user(user)
                .subtotal(subtotal)
                .discountAmount(discount)
                .shippingCharge(shipping)
                .totalAmount(total)
                .couponCode(couponCode)
                .shippingAddress(request.getShippingAddress())
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "CARD")
                .paymentStatus("COMPLETED") // simulated payment
                .trackingNumber("TRK-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase())
                .notes(request.getNotes())
                .build();

        // Add order items & deduct stock
        for (Cart item : cartItems) {
            Product p = item.getProduct();
            OrderItem oi = OrderItem.builder()
                    .order(order)
                    .product(p)
                    .productName(p.getName())
                    .productImage(p.getImageUrl())
                    .quantity(item.getQuantity())
                    .unitPrice(p.getCurrentPrice())
                    .totalPrice(p.getCurrentPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .build();
            order.getOrderItems().add(oi);
            p.setStockQuantity(p.getStockQuantity() - item.getQuantity());
            productRepository.save(p);
        }

        // Increment coupon usage
        if (couponCode != null && !couponCode.isBlank()) {
            couponService.incrementUsage(couponCode);
        }

        Order saved = orderRepository.save(order);
        cartRepository.deleteByUserId(user.getId());
        return toOrderResponse(saved);
    }

    public Page<OrderDTO.OrderResponse> getUserOrders(User user, int page, int size) {
        return orderRepository
                .findByUserId(user.getId(), PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(this::toOrderResponse);
    }

    public OrderDTO.OrderResponse getOrderById(Long id, User user) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        if (!order.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Access denied to this order");
        }
        return toOrderResponse(order);
    }

    @Transactional
    public OrderDTO.OrderResponse updateOrderStatus(Long id, Order.OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        order.setStatus(newStatus);
        if (newStatus == Order.OrderStatus.PACKED)
            order.setPackedAt(LocalDateTime.now());
        if (newStatus == Order.OrderStatus.SHIPPED)
            order.setShippedAt(LocalDateTime.now());
        if (newStatus == Order.OrderStatus.DELIVERED)
            order.setDeliveredAt(LocalDateTime.now());
        return toOrderResponse(orderRepository.save(order));
    }

    private OrderDTO.OrderResponse toOrderResponse(Order o) {
        List<OrderDTO.OrderItemResponse> items = o.getOrderItems().stream()
                .map(oi -> OrderDTO.OrderItemResponse.builder()
                        .id(oi.getId())
                        .productId(oi.getProduct().getId())
                        .productName(oi.getProductName())
                        .productImage(oi.getProductImage())
                        .quantity(oi.getQuantity())
                        .unitPrice(oi.getUnitPrice())
                        .totalPrice(oi.getTotalPrice())
                        .build())
                .collect(Collectors.toList());

        return OrderDTO.OrderResponse.builder()
                .id(o.getId())
                .orderNumber(o.getOrderNumber())
                .status(o.getStatus().name())
                .orderItems(items)
                .subtotal(o.getSubtotal())
                .discountAmount(o.getDiscountAmount())
                .shippingCharge(o.getShippingCharge())
                .totalAmount(o.getTotalAmount())
                .couponCode(o.getCouponCode())
                .shippingAddress(o.getShippingAddress())
                .paymentMethod(o.getPaymentMethod())
                .paymentStatus(o.getPaymentStatus())
                .trackingNumber(o.getTrackingNumber())
                .createdAt(o.getCreatedAt())
                .packedAt(o.getPackedAt())
                .shippedAt(o.getShippedAt())
                .deliveredAt(o.getDeliveredAt())
                .build();
    }
}
