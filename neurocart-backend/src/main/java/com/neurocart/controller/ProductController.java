package com.neurocart.controller;

import com.neurocart.dto.ProductDTO;
import com.neurocart.entity.User;
import com.neurocart.service.ProductService;
import com.neurocart.service.RecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final RecommendationService recommendationService;

    @GetMapping
    public ResponseEntity<Page<ProductDTO.ProductSummary>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(productService.getAllProducts(page, size, sortBy, categoryId, search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO.ProductResponse> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping("/create")
    public ResponseEntity<ProductDTO.ProductResponse> createProduct(
            @Valid @RequestBody ProductDTO.ProductRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(productService.createProduct(request, user));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ProductDTO.ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO.ProductRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(productService.updateProduct(id, request, user));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/featured")
    public ResponseEntity<List<ProductDTO.ProductSummary>> getFeatured() {
        return ResponseEntity.ok(productService.getFeaturedProducts());
    }

    @GetMapping("/{id}/similar")
    public ResponseEntity<List<ProductDTO.ProductSummary>> getSimilar(@PathVariable Long id) {
        return ResponseEntity.ok(recommendationService.getSimilarProducts(id));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<ProductDTO.ProductSummary>> getRecommendations(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(recommendationService.getRecommendations(user));
    }
}
