package com.neurocart.repository;

import com.neurocart.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {
    Optional<Vendor> findByUserId(Long userId);

    List<Vendor> findByStatus(Vendor.VendorStatus status);

    boolean existsByUserId(Long userId);
}
