package com.neurocart.repository;

import com.neurocart.entity.ProductView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductViewRepository extends JpaRepository<ProductView, Long> {
    Optional<ProductView> findByUserIdAndProductId(Long userId, Long productId);

    @Query("SELECT pv FROM ProductView pv WHERE pv.user.id = :userId ORDER BY pv.lastViewedAt DESC")
    List<ProductView> findRecentViewsByUserId(@Param("userId") Long userId,
            org.springframework.data.domain.Pageable pageable);

    @Query("SELECT pv.product.category.id, COUNT(pv) FROM ProductView pv WHERE pv.user.id = :userId GROUP BY pv.product.category.id ORDER BY COUNT(pv) DESC")
    List<Object[]> findCategoryInterestByUserId(@Param("userId") Long userId);
}
