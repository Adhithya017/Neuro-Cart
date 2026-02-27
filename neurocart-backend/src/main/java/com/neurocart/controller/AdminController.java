package com.neurocart.controller;

import com.neurocart.dto.AnalyticsDTO;
import com.neurocart.entity.Vendor;
import com.neurocart.service.AdminService;
import com.neurocart.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final ProductService productService;

    @GetMapping("/dashboard")
    public ResponseEntity<AnalyticsDTO.DashboardStats> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/analytics/revenue")
    public ResponseEntity<List<AnalyticsDTO.MonthlyRevenue>> getMonthlyRevenue(
            @RequestParam(defaultValue = "2025") int year) {
        return ResponseEntity.ok(adminService.getMonthlyRevenue(year));
    }

    @GetMapping("/analytics/top-products")
    public ResponseEntity<List<AnalyticsDTO.TopProduct>> getTopProducts(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(adminService.getTopSellingProducts(limit));
    }

    @GetMapping("/vendors/pending")
    public ResponseEntity<List<Vendor>> getPendingVendors() {
        return ResponseEntity.ok(adminService.getPendingVendors());
    }

    @PostMapping("/vendors/{id}/approve")
    public ResponseEntity<Vendor> approveVendor(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.approveVendor(id));
    }

    @PostMapping("/vendors/{id}/reject")
    public ResponseEntity<Vendor> rejectVendor(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.rejectVendor(id));
    }

    @GetMapping("/products/low-stock")
    public ResponseEntity<?> getLowStock(@RequestParam(defaultValue = "10") int threshold) {
        return ResponseEntity.ok(productService.getLowStockProducts(threshold));
    }
}
