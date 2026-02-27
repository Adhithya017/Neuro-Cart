package com.neurocart.controller;

import com.neurocart.entity.Wishlist;
import com.neurocart.entity.User;
import com.neurocart.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<List<Wishlist>> getWishlist(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(wishlistService.getWishlist(user));
    }

    @PostMapping("/add/{productId}")
    public ResponseEntity<Wishlist> addToWishlist(@PathVariable Long productId, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(wishlistService.addToWishlist(user, productId));
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<Void> removeFromWishlist(@PathVariable Long productId, @AuthenticationPrincipal User user) {
        wishlistService.removeFromWishlist(user, productId);
        return ResponseEntity.noContent().build();
    }
}
