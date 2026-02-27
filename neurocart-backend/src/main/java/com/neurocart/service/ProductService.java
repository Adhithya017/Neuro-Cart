package com.neurocart.service;

import com.neurocart.dto.ProductDTO;
import com.neurocart.entity.Category;
import com.neurocart.entity.Product;
import com.neurocart.entity.User;
import com.neurocart.entity.Vendor;
import com.neurocart.exception.BadRequestException;
import com.neurocart.exception.ResourceNotFoundException;
import com.neurocart.repository.CategoryRepository;
import com.neurocart.repository.ProductRepository;
import com.neurocart.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final VendorRepository vendorRepository;

    // ── Dynamic Pricing Engine ────────────────────────────────────────────────
    public BigDecimal calculateDynamicPrice(Product product) {
        BigDecimal price = product.getBasePrice();

        // Apply coupon/offer discount
        if (product.getDiscountPercentage() != null && product.getDiscountPercentage().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = price.multiply(product.getDiscountPercentage()).divide(BigDecimal.valueOf(100));
            price = price.subtract(discount);
        }

        // Stock scarcity surge (< 10 units → 5-15% premium)
        if (product.getStockQuantity() < 10 && product.getStockQuantity() > 0) {
            double surgeFactor = 1.0 + (0.15 * (1.0 - product.getStockQuantity() / 10.0));
            price = price.multiply(BigDecimal.valueOf(surgeFactor)).setScale(2, java.math.RoundingMode.HALF_UP);
        }

        // Demand-based surge (demand > 100 → up to 10% premium)
        if (product.getDemandCount() > 100) {
            double demandFactor = Math.min(1.10, 1 + (product.getDemandCount() - 100.0) / 2000.0);
            price = price.multiply(BigDecimal.valueOf(demandFactor)).setScale(2, java.math.RoundingMode.HALF_UP);
        }

        return price.max(product.getBasePrice().multiply(BigDecimal.valueOf(0.5))); // floor: 50% of base
    }

    public Page<ProductDTO.ProductSummary> getAllProducts(int page, int size, String sortBy, Long categoryId,
            String search) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, sortBy == null ? "createdAt" : sortBy));

        Page<Product> products;
        if (search != null && !search.isBlank()) {
            products = productRepository.searchProducts(search.trim(), pageable);
        } else if (categoryId != null) {
            products = productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable);
        } else {
            products = productRepository.findByActiveTrue(pageable);
        }

        return products.map(p -> toSummary(p, false));
    }

    public ProductDTO.ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        // Increment demand count
        product.setDemandCount(product.getDemandCount() + 1);
        product.setCurrentPrice(calculateDynamicPrice(product));
        productRepository.save(product);
        return toResponse(product, false, false);
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductDTO.ProductResponse createProduct(ProductDTO.ProductRequest request, User currentUser) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));

        Vendor vendor = vendorRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new BadRequestException("User is not a vendor"));

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .basePrice(request.getBasePrice())
                .currentPrice(request.getBasePrice())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .additionalImages(request.getAdditionalImages())
                .sku(request.getSku())
                .weightKg(request.getWeightKg())
                .featured(request.isFeatured())
                .discountPercentage(
                        request.getDiscountPercentage() != null ? request.getDiscountPercentage() : BigDecimal.ZERO)
                .tags(request.getTags())
                .category(category)
                .vendor(vendor)
                .build();

        product.setCurrentPrice(calculateDynamicPrice(product));
        return toResponse(productRepository.save(product), false, false);
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductDTO.ProductResponse updateProduct(Long id, ProductDTO.ProductRequest request, User currentUser) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBasePrice(request.getBasePrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrl(request.getImageUrl());
        product.setAdditionalImages(request.getAdditionalImages());
        product.setSku(request.getSku());
        product.setFeatured(request.isFeatured());
        product.setDiscountPercentage(
                request.getDiscountPercentage() != null ? request.getDiscountPercentage() : BigDecimal.ZERO);
        product.setTags(request.getTags());
        product.setCategory(category);
        product.setCurrentPrice(calculateDynamicPrice(product));

        return toResponse(productRepository.save(product), false, false);
    }

    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        product.setActive(false);
        productRepository.save(product);
    }

    @Cacheable("featuredProducts")
    public List<ProductDTO.ProductSummary> getFeaturedProducts() {
        return productRepository.findByFeaturedTrueAndActiveTrueOrderByCreatedAtDesc()
                .stream().map(p -> toSummary(p, false)).collect(Collectors.toList());
    }

    public List<ProductDTO.ProductSummary> getLowStockProducts(int threshold) {
        return productRepository.findLowStockProducts(threshold)
                .stream().map(p -> toSummary(p, false)).collect(Collectors.toList());
    }

    public void updateProductRating(Long productId) {
        // Called after a review is added
        Product product = productRepository.findById(productId).orElseThrow();
        productRepository.save(product);
    }

    // ── Mappers ───────────────────────────────────────────────────────────────
    public ProductDTO.ProductSummary toSummary(Product p, boolean inWishlist) {
        return ProductDTO.ProductSummary.builder()
                .id(p.getId())
                .name(p.getName())
                .currentPrice(p.getCurrentPrice())
                .basePrice(p.getBasePrice())
                .imageUrl(p.getImageUrl())
                .averageRating(p.getAverageRating())
                .totalReviews(p.getTotalReviews())
                .stockQuantity(p.getStockQuantity())
                .discountPercentage(p.getDiscountPercentage())
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .inWishlist(inWishlist)
                .build();
    }

    public ProductDTO.ProductResponse toResponse(Product p, boolean inWishlist, boolean inCart) {
        return ProductDTO.ProductResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .basePrice(p.getBasePrice())
                .currentPrice(p.getCurrentPrice())
                .stockQuantity(p.getStockQuantity())
                .demandCount(p.getDemandCount())
                .imageUrl(p.getImageUrl())
                .additionalImages(p.getAdditionalImages())
                .sku(p.getSku())
                .active(p.isActive())
                .featured(p.isFeatured())
                .averageRating(p.getAverageRating())
                .totalReviews(p.getTotalReviews())
                .discountPercentage(p.getDiscountPercentage())
                .tags(p.getTags())
                .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .vendorId(p.getVendor() != null ? p.getVendor().getId() : null)
                .vendorName(p.getVendor() != null ? p.getVendor().getBusinessName() : null)
                .createdAt(p.getCreatedAt())
                .inWishlist(inWishlist)
                .inCart(inCart)
                .build();
    }
}
