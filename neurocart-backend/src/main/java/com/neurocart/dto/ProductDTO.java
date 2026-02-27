package com.neurocart.dto;

import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ProductDTO {

    @Data
    @Builder
    public static class ProductRequest {
        @NotBlank(message = "Product name is required")
        private String name;
        private String description;

        @NotNull
        @Positive
        private BigDecimal basePrice;

        @NotNull
        @Positive
        private Integer stockQuantity;

        private String imageUrl;
        private List<String> additionalImages;
        private String sku;
        private BigDecimal weightKg;
        private boolean featured;
        private BigDecimal discountPercentage;
        private String tags;

        @NotNull
        private Long categoryId;
    }

    @Data
    @Builder
    public static class ProductResponse {
        private Long id;
        private String name;
        private String description;
        private BigDecimal basePrice;
        private BigDecimal currentPrice;
        private Integer stockQuantity;
        private Integer demandCount;
        private String imageUrl;
        private List<String> additionalImages;
        private String sku;
        private boolean active;
        private boolean featured;
        private Double averageRating;
        private Integer totalReviews;
        private BigDecimal discountPercentage;
        private String tags;
        private Long categoryId;
        private String categoryName;
        private Long vendorId;
        private String vendorName;
        private LocalDateTime createdAt;
        private boolean inWishlist;
        private boolean inCart;
    }

    @Data
    @Builder
    public static class ProductSummary {
        private Long id;
        private String name;
        private BigDecimal currentPrice;
        private BigDecimal basePrice;
        private String imageUrl;
        private Double averageRating;
        private Integer totalReviews;
        private Integer stockQuantity;
        private BigDecimal discountPercentage;
        private String categoryName;
        private boolean inWishlist;
    }
}
