package com.neurocart.repository;

import com.neurocart.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByActiveTrue(Pageable pageable);

    Page<Product> findByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);

    Page<Product> findByVendorIdAndActiveTrue(Long vendorId, Pageable pageable);

    List<Product> findByFeaturedTrueAndActiveTrueOrderByCreatedAtDesc();

    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
            "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.tags) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.stockQuantity <= :threshold ORDER BY p.stockQuantity ASC")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);

    @Query("SELECT p FROM Product p WHERE p.active = true ORDER BY p.demandCount DESC")
    List<Product> findTopByDemand(Pageable pageable);

    List<Product> findByCategoryIdAndActiveTrueAndIdNot(Long categoryId, Long productId);

    @Query("SELECT p FROM Product p WHERE p.active = true ORDER BY p.averageRating DESC")
    List<Product> findTopRatedProducts(Pageable pageable);
}
