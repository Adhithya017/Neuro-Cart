package com.neurocart.service;

import com.neurocart.dto.AnalyticsDTO;
import com.neurocart.entity.Vendor;
import com.neurocart.exception.ResourceNotFoundException;
import com.neurocart.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final VendorRepository vendorRepository;

    public AnalyticsDTO.DashboardStats getDashboardStats() {
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
        LocalDateTime now = LocalDateTime.now();

        return AnalyticsDTO.DashboardStats.builder()
                .totalUsers(userRepository.count())
                .totalOrders(orderRepository.count())
                .totalRevenue(orderRepository.sumRevenueByDateRange(LocalDateTime.of(2000, 1, 1, 0, 0), now))
                .totalProducts(productRepository.count())
                .pendingVendors((long) vendorRepository.findByStatus(Vendor.VendorStatus.PENDING).size())
                .lowStockProducts((long) productRepository.findLowStockProducts(10).size())
                .thisMonthOrders(orderRepository.countByCreatedAtBetween(monthStart, now))
                .thisMonthRevenue(orderRepository.sumRevenueByDateRange(monthStart, now))
                .build();
    }

    public List<AnalyticsDTO.MonthlyRevenue> getMonthlyRevenue(int year) {
        List<Object[]> rows = orderRepository.monthlyRevenue(year);
        List<AnalyticsDTO.MonthlyRevenue> result = new ArrayList<>();
        for (Object[] row : rows) {
            int month = ((Number) row[0]).intValue();
            BigDecimal revenue = (BigDecimal) row[1];
            result.add(AnalyticsDTO.MonthlyRevenue.builder()
                    .month(month)
                    .monthName(Month.of(month).name())
                    .revenue(revenue)
                    .build());
        }
        return result;
    }

    public List<AnalyticsDTO.TopProduct> getTopSellingProducts(int limit) {
        return orderRepository.topSellingProducts(PageRequest.of(0, limit)).stream()
                .map(row -> AnalyticsDTO.TopProduct.builder()
                        .productId(((Number) row[0]).longValue())
                        .productName((String) row[1])
                        .totalSold(((Number) row[2]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    public List<Vendor> getPendingVendors() {
        return vendorRepository.findByStatus(Vendor.VendorStatus.PENDING);
    }

    @Transactional
    public Vendor approveVendor(Long vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", vendorId));
        vendor.setStatus(Vendor.VendorStatus.APPROVED);
        return vendorRepository.save(vendor);
    }

    @Transactional
    public Vendor rejectVendor(Long vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", vendorId));
        vendor.setStatus(Vendor.VendorStatus.REJECTED);
        return vendorRepository.save(vendor);
    }
}
