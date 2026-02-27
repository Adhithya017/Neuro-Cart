package com.neurocart.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

public class AnalyticsDTO {

    @Data
    @Builder
    public static class DashboardStats {
        private Long totalUsers;
        private Long totalOrders;
        private BigDecimal totalRevenue;
        private Long totalProducts;
        private Long pendingVendors;
        private Long lowStockProducts;
        private Long thisMonthOrders;
        private BigDecimal thisMonthRevenue;
    }

    @Data
    @Builder
    public static class MonthlyRevenue {
        private int month;
        private String monthName;
        private BigDecimal revenue;
        private Long orderCount;
    }

    @Data
    @Builder
    public static class TopProduct {
        private Long productId;
        private String productName;
        private String productImage;
        private Long totalSold;
        private BigDecimal revenue;
    }

    @Data
    @Builder
    public static class VendorAnalytics {
        private Long vendorId;
        private String businessName;
        private Long totalProducts;
        private Long totalOrders;
        private BigDecimal totalRevenue;
        private List<TopProduct> topProducts;
    }
}
