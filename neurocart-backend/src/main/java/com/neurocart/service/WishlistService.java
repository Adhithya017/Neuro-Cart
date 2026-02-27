package com.neurocart.service;

import com.neurocart.entity.Wishlist;
import com.neurocart.entity.Product;
import com.neurocart.entity.User;
import com.neurocart.exception.BadRequestException;
import com.neurocart.exception.ResourceNotFoundException;
import com.neurocart.repository.ProductRepository;
import com.neurocart.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;

    public List<Wishlist> getWishlist(User user) {
        return wishlistRepository.findByUserId(user.getId());
    }

    @Transactional
    public Wishlist addToWishlist(User user, Long productId) {
        if (wishlistRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            throw new BadRequestException("Product already in wishlist");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        return wishlistRepository.save(Wishlist.builder().user(user).product(product).build());
    }

    @Transactional
    public void removeFromWishlist(User user, Long productId) {
        wishlistRepository.deleteByUserIdAndProductId(user.getId(), productId);
    }

    public boolean isInWishlist(User user, Long productId) {
        return wishlistRepository.existsByUserIdAndProductId(user.getId(), productId);
    }
}
