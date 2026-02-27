package com.neurocart.dto;

import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public class CartDTO {

    @Data
    public static class CartRequest {
        @NotNull
        private Long productId;
        @Min(1)
        @Max(50)
        private Integer quantity = 1;
    }

    @Data
    @Builder
    public static class CartResponse {
        private Long id;
        private Long productId;
        private String productName;
        private String productImage;
        private BigDecimal unitPrice;
        private Integer quantity;
        private BigDecimal totalPrice;
        private Integer availableStock;
        private boolean stockWarning;
    }

    @Data
    @Builder
    public static class CartSummary {
        private List<CartResponse> items;
        private BigDecimal subtotal;
        private int itemCount;
        private List<SmartSuggestion> alternativeSuggestions;
        private List<BundleSuggestion> bundleSuggestions;
    }

    @Data
    @Builder
    public static class SmartSuggestion {
        private Long originalProductId;
        private String originalProductName;
        private Long alternativeProductId;
        private String alternativeProductName;
        private String alternativeImage;
        private BigDecimal originalPrice;
        private BigDecimal alternativePrice;
        private BigDecimal savings;
    }

    @Data
    @Builder
    public static class BundleSuggestion {
        private String bundleName;
        private List<Long> productIds;
        private List<String> productNames;
        private BigDecimal originalTotal;
        private BigDecimal bundlePrice;
        private BigDecimal savings;
    }
}
