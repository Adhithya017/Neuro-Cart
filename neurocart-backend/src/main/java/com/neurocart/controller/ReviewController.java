package com.neurocart.controller;

import com.neurocart.entity.Review;
import com.neurocart.entity.User;
import com.neurocart.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/{productId}")
    public ResponseEntity<Page<Review>> getReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.getProductReviews(productId, page, size));
    }

    @PostMapping("/{productId}")
    public ResponseEntity<Review> addReview(
            @PathVariable Long productId,
            @RequestBody ReviewService.ReviewRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(reviewService.addReview(user, productId, request));
    }
}
