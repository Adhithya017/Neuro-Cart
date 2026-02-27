package com.neurocart.service;

import com.neurocart.entity.Product;
import com.neurocart.entity.Review;
import com.neurocart.entity.User;
import com.neurocart.exception.BadRequestException;
import com.neurocart.exception.ResourceNotFoundException;
import com.neurocart.repository.ProductRepository;
import com.neurocart.repository.ReviewRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.constraints.*;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    public Page<Review> getProductReviews(Long productId, int page, int size) {
        return reviewRepository.findByProductId(productId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @Transactional
    public Review addReview(User user, Long productId, ReviewRequest request) {
        if (reviewRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            throw new BadRequestException("You have already reviewed this product");
        }
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new BadRequestException("Rating must be between 1 and 5");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(request.getRating())
                .comment(request.getComment())
                .verifiedPurchase(request.isVerifiedPurchase())
                .build();
        Review saved = reviewRepository.save(review);

        // Update product average rating
        Double newAvg = reviewRepository.findAverageRatingByProductId(productId);
        Long count = reviewRepository.countByProductId(productId);
        product.setAverageRating(newAvg != null ? Math.round(newAvg * 10.0) / 10.0 : 0.0);
        product.setTotalReviews(count != null ? count.intValue() : 0);
        productRepository.save(product);

        return saved;
    }

    @Data
    public static class ReviewRequest {
        @Min(1)
        @Max(5)
        private int rating;
        private String comment;
        private boolean verifiedPurchase;
    }
}
