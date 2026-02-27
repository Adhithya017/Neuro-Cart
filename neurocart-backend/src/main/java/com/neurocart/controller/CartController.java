package com.neurocart.controller;

import com.neurocart.dto.CartDTO;
import com.neurocart.entity.User;
import com.neurocart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartDTO.CartSummary> getCart(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(cartService.getCart(user));
    }

    @PostMapping("/add")
    public ResponseEntity<CartDTO.CartResponse> addToCart(
            @RequestBody CartDTO.CartRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(cartService.addToCart(user, request));
    }

    @PutMapping("/update/{productId}")
    public ResponseEntity<CartDTO.CartResponse> updateCart(
            @PathVariable Long productId,
            @RequestParam int quantity,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(cartService.updateCartItem(user, productId, quantity));
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<Void> removeFromCart(@PathVariable Long productId, @AuthenticationPrincipal User user) {
        cartService.removeFromCart(user, productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal User user) {
        cartService.clearCart(user);
        return ResponseEntity.noContent().build();
    }
}
