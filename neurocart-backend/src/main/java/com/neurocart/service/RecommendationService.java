package com.neurocart.service;

import com.neurocart.dto.ProductDTO;
import com.neurocart.entity.Product;
import com.neurocart.entity.ProductView;
import com.neurocart.entity.User;
import com.neurocart.repository.ProductRepository;
import com.neurocart.repository.ProductViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final ProductViewRepository productViewRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    @Transactional
    public void recordProductView(User user, Product product) {
        ProductView view = productViewRepository
                .findByUserIdAndProductId(user.getId(), product.getId())
                .orElse(ProductView.builder().user(user).product(product).viewCount(0).build());
        view.setViewCount(view.getViewCount() + 1);
        productViewRepository.save(view);
    }

    public List<ProductDTO.ProductSummary> getRecommendations(User user) {
        List<ProductDTO.ProductSummary> recommendations = new ArrayList<>();

        // 1. Recently viewed products' categories
        List<ProductView> recentViews = productViewRepository.findRecentViewsByUserId(user.getId(),
                PageRequest.of(0, 5));
        recentViews.stream()
                .map(view -> view.getProduct().getCategory().getId())
                .distinct()
                .limit(3)
                .forEach(catId -> {
                    List<Product> catProducts = productRepository
                            .findByCategoryIdAndActiveTrue(catId, PageRequest.of(0, 4))
                            .getContent();
                    catProducts.forEach(p -> {
                        if (recommendations.stream().noneMatch(r -> r.getId().equals(p.getId()))) {
                            recommendations.add(productService.toSummary(p, false));
                        }
                    });
                });

        // 2. Top demand products as fallback
        if (recommendations.size() < 8) {
            List<Product> topDemand = productRepository.findTopByDemand(PageRequest.of(0, 8));
            topDemand.forEach(p -> {
                if (recommendations.stream().noneMatch(r -> r.getId().equals(p.getId()))) {
                    recommendations.add(productService.toSummary(p, false));
                }
            });
        }

        return recommendations.stream().limit(8).collect(Collectors.toList());
    }

    public List<ProductDTO.ProductSummary> getSimilarProducts(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow();
        return productRepository
                .findByCategoryIdAndActiveTrueAndIdNot(product.getCategory().getId(), productId)
                .stream().limit(6).map(p -> productService.toSummary(p, false)).collect(Collectors.toList());
    }
}
