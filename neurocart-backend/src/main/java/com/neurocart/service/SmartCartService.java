package com.neurocart.service;

import com.neurocart.dto.CartDTO;
import com.neurocart.entity.Cart;
import com.neurocart.entity.Product;
import com.neurocart.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SmartCartService {

    private final ProductRepository productRepository;

    public List<CartDTO.SmartSuggestion> findAlternatives(List<Cart> cartItems) {
        List<CartDTO.SmartSuggestion> suggestions = new ArrayList<>();
        for (Cart item : cartItems) {
            Product original = item.getProduct();
            // Find cheaper alternatives in same category
            List<Product> alternatives = productRepository
                    .findByCategoryIdAndActiveTrueAndIdNot(original.getCategory().getId(), original.getId());

            alternatives.stream()
                    .filter(alt -> alt.getCurrentPrice().compareTo(original.getCurrentPrice()) < 0)
                    .findFirst()
                    .ifPresent(alt -> {
                        BigDecimal savings = original.getCurrentPrice().subtract(alt.getCurrentPrice());
                        suggestions.add(CartDTO.SmartSuggestion.builder()
                                .originalProductId(original.getId())
                                .originalProductName(original.getName())
                                .alternativeProductId(alt.getId())
                                .alternativeProductName(alt.getName())
                                .alternativeImage(alt.getImageUrl())
                                .originalPrice(original.getCurrentPrice())
                                .alternativePrice(alt.getCurrentPrice())
                                .savings(savings)
                                .build());
                    });
        }
        return suggestions;
    }

    public List<CartDTO.BundleSuggestion> findBundles(List<Cart> cartItems) {
        List<CartDTO.BundleSuggestion> bundles = new ArrayList<>();
        if (cartItems.isEmpty())
            return bundles;

        // Suggest a "Popular Bundle" from the category of the first cart item
        Product firstProduct = cartItems.get(0).getProduct();
        List<Product> bundleProducts = productRepository
                .findByCategoryIdAndActiveTrueAndIdNot(firstProduct.getCategory().getId(), firstProduct.getId());

        if (!bundleProducts.isEmpty()) {
            Product bundleItem = bundleProducts.get(0);
            BigDecimal originalTotal = firstProduct.getCurrentPrice().add(bundleItem.getCurrentPrice());
            BigDecimal bundlePrice = originalTotal.multiply(BigDecimal.valueOf(0.90)).setScale(2,
                    java.math.RoundingMode.HALF_UP);
            BigDecimal savings = originalTotal.subtract(bundlePrice);

            bundles.add(CartDTO.BundleSuggestion.builder()
                    .bundleName("🔥 Popular Bundle - Save 10%")
                    .productIds(List.of(firstProduct.getId(), bundleItem.getId()))
                    .productNames(List.of(firstProduct.getName(), bundleItem.getName()))
                    .originalTotal(originalTotal)
                    .bundlePrice(bundlePrice)
                    .savings(savings)
                    .build());
        }
        return bundles;
    }
}
