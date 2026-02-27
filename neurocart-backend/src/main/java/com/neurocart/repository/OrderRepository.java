package com.neurocart.repository;

import com.neurocart.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUserId(Long userId, Pageable pageable);

    List<Order> findByUserId(Long userId);

    Optional<Order> findByOrderNumber(String orderNumber);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.createdAt BETWEEN :start AND :end AND o.status != 'CANCELLED'")
    BigDecimal sumRevenueByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT MONTH(o.createdAt), SUM(o.totalAmount) FROM Order o WHERE YEAR(o.createdAt) = :year AND o.status != 'CANCELLED' GROUP BY MONTH(o.createdAt)")
    List<Object[]> monthlyRevenue(@Param("year") int year);

    @Query("SELECT oi.product.id, oi.product.name, SUM(oi.quantity) as totalQty FROM OrderItem oi GROUP BY oi.product.id, oi.product.name ORDER BY totalQty DESC")
    List<Object[]> topSellingProducts(Pageable pageable);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
