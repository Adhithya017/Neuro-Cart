package com.neurocart.service;

import com.neurocart.dto.CartDTO;
import com.neurocart.entity.Cart;
import com.neurocart.entity.Product;
import com.neurocart.entity.User;
import com.neurocart.exception.BadRequestException;
import com.neurocart.exception.ResourceNotFoundException;
import com.neurocart.repository.CartRepository;
import com.neurocart.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final SmartCartService smartCartService;

    public CartDTO.CartSummary getCart(User user) {
        List<Cart> cartItems = cartRepository.findByUserId(user.getId());
        List<CartDTO.CartResponse> responses = cartItems.stream().map(this::toCartResponse)
                .collect(Collectors.toList());
        BigDecimal subtotal = responses.stream()
                .map(CartDTO.CartResponse::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Smart cart optimization
        List<CartDTO.SmartSuggestion> alternatives = smartCartService.findAlternatives(cartItems);
        List<CartDTO.BundleSuggestion> bundles = smartCartService.findBundles(cartItems);

        return CartDTO.CartSummary.builder()
                .items(responses)
                .subtotal(subtotal)
                .itemCount(cartItems.size())
                .alternativeSuggestions(alternatives)
                .bundleSuggestions(bundles)
                .build();
    }

    @Transactional
    public CartDTO.CartResponse addToCart(User user, CartDTO.CartRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));

        if (!product.isActive())
            throw new BadRequestException("Product is no longer available");
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new BadRequestException("Only " + product.getStockQuantity() + " units available in stock");
        }

        Cart cart = cartRepository.findByUserIdAndProductId(user.getId(), product.getId())
                .orElse(Cart.builder().user(user).product(product).build());

        int newQty = cart.getId() != null ? cart.getQuantity() + request.getQuantity() : request.getQuantity();
        if (newQty > product.getStockQuantity()) {
            throw new BadRequestException(
                    "Cannot add more than available stock (" + product.getStockQuantity() + " units)");
        }

        cart.setQuantity(newQty);
        cart.setPriceSnapshot(product.getCurrentPrice());
        return toCartResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartDTO.CartResponse updateCartItem(User user, Long productId, int quantity) {
        Cart cart = cartRepository.findByUserIdAndProductId(user.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        Product product = cart.getProduct();
        if (quantity > product.getStockQuantity()) {
            throw new BadRequestException("Only " + product.getStockQuantity() + " units available");
        }
        cart.setQuantity(quantity);
        cart.setPriceSnapshot(product.getCurrentPrice());
        return toCartResponse(cartRepository.save(cart));
    }

    @Transactional
    public void removeFromCart(User user, Long productId) {
        cartRepository.deleteByUserIdAndProductId(user.getId(), productId);
    }

    @Transactional
    public void clearCart(User user) {
        cartRepository.deleteByUserId(user.getId());
    }

    public List<Cart> getUserCartItems(User user) {
        return cartRepository.findByUserId(user.getId());
    }

    private CartDTO.CartResponse toCartResponse(Cart cart) {
        Product p = cart.getProduct();
        BigDecimal unitPrice = p.getCurrentPrice();
        BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(cart.getQuantity()));
        boolean stockWarning = p.getStockQuantity() <= 5;
        return CartDTO.CartResponse.builder()
                .id(cart.getId())
                .productId(p.getId())
                .productName(p.getName())
                .productImage(p.getImageUrl())
                .unitPrice(unitPrice)
                .quantity(cart.getQuantity())
                .totalPrice(total)
                .availableStock(p.getStockQuantity())
                .stockWarning(stockWarning)
                .build();
    }
}
