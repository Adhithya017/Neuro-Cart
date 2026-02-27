package com.neurocart.controller;

import com.neurocart.dto.OrderDTO;
import com.neurocart.entity.Order;
import com.neurocart.entity.User;
import com.neurocart.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/place")
    public ResponseEntity<OrderDTO.OrderResponse> placeOrder(
            @RequestBody OrderDTO.PlaceOrderRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(orderService.placeOrder(user, request));
    }

    @GetMapping
    public ResponseEntity<Page<OrderDTO.OrderResponse>> getUserOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(orderService.getUserOrders(user, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO.OrderResponse> getOrder(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(orderService.getOrderById(id, user));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDTO.OrderResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, Order.OrderStatus.valueOf(status)));
    }
}
