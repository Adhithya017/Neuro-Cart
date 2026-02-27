package com.neurocart.config;

import com.neurocart.entity.*;
import com.neurocart.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

        private final UserRepository userRepository;
        private final RoleRepository roleRepository;
        private final CategoryRepository categoryRepository;
        private final VendorRepository vendorRepository;
        private final ProductRepository productRepository;
        private final CouponRepository couponRepository;
        private final PasswordEncoder passwordEncoder;

        @Override
        @Transactional
        public void run(String... args) {
                if (roleRepository.count() > 0) {
                        log.info("Data already initialized. Skipping...");
                        return;
                }

                log.info("Initializing NeuroCart seed data...");

                // ── Roles ─────────────────────────────────────────────────────────────
                Role adminRole = roleRepository.save(Role.builder().name(Role.RoleName.ROLE_ADMIN).build());
                Role vendorRole = roleRepository.save(Role.builder().name(Role.RoleName.ROLE_VENDOR).build());
                Role customerRole = roleRepository.save(Role.builder().name(Role.RoleName.ROLE_CUSTOMER).build());

                // ── Admin User ─────────────────────────────────────────────────────────
                userRepository.save(User.builder()
                                .username("admin")
                                .email("admin@neurocart.com")
                                .password(passwordEncoder.encode("Admin@123"))
                                .fullName("NeuroCart Admin")
                                .roles(Set.of(adminRole))
                                .build());

                // ── Vendor Users ───────────────────────────────────────────────────────
                User vendorUser1 = userRepository.save(User.builder()
                                .username("techzone")
                                .email("techzone@vendor.com")
                                .password(passwordEncoder.encode("Vendor@123"))
                                .fullName("Tech Zone Manager")
                                .roles(Set.of(vendorRole))
                                .build());

                User vendorUser2 = userRepository.save(User.builder()
                                .username("fashionhub")
                                .email("fashionhub@vendor.com")
                                .password(passwordEncoder.encode("Vendor@123"))
                                .fullName("Fashion Hub Manager")
                                .roles(Set.of(vendorRole))
                                .build());

                // ── Customer Users ─────────────────────────────────────────────────────
                userRepository.save(User.builder()
                                .username("john_doe")
                                .email("john@customer.com")
                                .password(passwordEncoder.encode("Test@123"))
                                .fullName("John Doe")
                                .phone("+91-9876543210")
                                .roles(Set.of(customerRole))
                                .build());

                userRepository.save(User.builder()
                                .username("jane_smith")
                                .email("jane@customer.com")
                                .password(passwordEncoder.encode("Test@123"))
                                .fullName("Jane Smith")
                                .phone("+91-9876543211")
                                .roles(Set.of(customerRole))
                                .build());

                // ── Vendors ───────────────────────────────────────────────────────────
                Vendor vendor1 = vendorRepository.save(Vendor.builder()
                                .user(vendorUser1)
                                .businessName("Tech Zone Electronics")
                                .businessEmail("info@techzone.com")
                                .description("Premium electronics and gadgets")
                                .status(Vendor.VendorStatus.APPROVED)
                                .build());

                Vendor vendor2 = vendorRepository.save(Vendor.builder()
                                .user(vendorUser2)
                                .businessName("Fashion Hub")
                                .businessEmail("info@fashionhub.com")
                                .description("Trendy fashion and accessories")
                                .status(Vendor.VendorStatus.APPROVED)
                                .build());

                // ── Categories ─────────────────────────────────────────────────────────
                Category electronics = categoryRepository
                                .save(Category.builder().name("Electronics").description("Gadgets & Devices")
                                                .imageUrl("https://images.unsplash.com/photo-1498049794561-7780e7231661?w=400")
                                                .iconClass("fa-laptop").build());
                Category fashion = categoryRepository
                                .save(Category.builder().name("Fashion").description("Clothing & Accessories")
                                                .imageUrl("https://images.unsplash.com/photo-1445205170230-053b83016050?w=400")
                                                .iconClass("fa-shirt").build());
                Category homeAppliances = categoryRepository
                                .save(Category.builder().name("Home & Appliances").description("Home essentials")
                                                .imageUrl("https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=400")
                                                .iconClass("fa-house").build());
                categoryRepository.save(Category.builder().name("Books").description("Knowledge & Learning")
                                .imageUrl("https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=400")
                                .iconClass("fa-book")
                                .build());
                Category sports = categoryRepository.save(Category.builder().name("Sports")
                                .description("Fitness & Sports Gear")
                                .imageUrl("https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=400")
                                .iconClass("fa-football")
                                .build());

                // ── Products ──────────────────────────────────────────────────────────
                createProduct("iPhone 15 Pro Max",
                                "Apple's flagship smartphone with 48MP camera, A17 Pro chip, titanium design",
                                new BigDecimal("134900"),
                                50, true, new BigDecimal("5"), electronics, vendor1,
                                "https://images.unsplash.com/photo-1696446701796-da61225697cc?w=400", 245);
                createProduct("Samsung Galaxy S24 Ultra",
                                "Quad-camera system, 200MP, built-in S Pen, Snapdragon 8 Gen 3",
                                new BigDecimal("124999"), 35, true, new BigDecimal("8"), electronics, vendor1,
                                "https://images.unsplash.com/photo-1706176942334-5e3fc5ae8ec5?w=400", 189);
                createProduct("MacBook Pro 14 M3", "Apple M3 chip, 14-inch Liquid Retina XDR display, 18hr battery",
                                new BigDecimal("199900"), 20, true, new BigDecimal("3"), electronics, vendor1,
                                "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=400", 156);
                createProduct("Sony WH-1000XM5", "Industry-leading noise cancelling headphones, 30hr battery",
                                new BigDecimal("29990"), 75, false, new BigDecimal("15"), electronics, vendor1,
                                "https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=400", 312);
                createProduct("iPad Pro 12.9 M2", "Apple M2, Liquid Retina XDR, ProMotion 120Hz display",
                                new BigDecimal("112900"), 30, true, new BigDecimal("0"), electronics, vendor1,
                                "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=400", 98);
                createProduct("Dell XPS 15", "Intel Core i9, OLED 4K display, RTX 4060, 16GB RAM",
                                new BigDecimal("159990"), 15,
                                false, new BigDecimal("10"), electronics, vendor1,
                                "https://images.unsplash.com/photo-1593642632559-0c6d3fc62b89?w=400", 87);

                createProduct("Premium Leather Jacket", "Genuine leather biker jacket, slim fit, multiple pockets",
                                new BigDecimal("8999"), 100, true, new BigDecimal("20"), fashion, vendor2,
                                "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=400", 423);
                createProduct("Designer Handbag", "Italian leather handbag, gold hardware, removable strap",
                                new BigDecimal("12499"), 45, false, new BigDecimal("0"), fashion, vendor2,
                                "https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=400", 267);
                createProduct("Running Shoes Pro", "Advanced cushioning, breathable mesh, carbon fiber plate",
                                new BigDecimal("7999"), 120, true, new BigDecimal("12"), sports, vendor2,
                                "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400", 534);
                createProduct("Yoga Mat Premium", "Non-slip, 6mm thick, eco-friendly TPE material",
                                new BigDecimal("1499"), 200,
                                false, new BigDecimal("0"), sports, vendor2,
                                "https://images.unsplash.com/photo-1592432678016-e910b452f9a2?w=400", 189);
                createProduct("Smart Home Hub", "Control all smart devices, compatible with Alexa & Google Home",
                                new BigDecimal("5999"), 8, true, new BigDecimal("5"), homeAppliances, vendor1,
                                "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400", 145);
                createProduct("Air Purifier HEPA", "True HEPA filter, covers 500 sq ft, PM2.5 sensor",
                                new BigDecimal("15999"),
                                3, false, new BigDecimal("0"), homeAppliances, vendor1,
                                "https://images.unsplash.com/photo-1585771724684-38269d6639fd?w=400", 78);

                // ── Coupons ───────────────────────────────────────────────────────────
                couponRepository.save(Coupon.builder()
                                .code("NEURO10")
                                .description("10% off on all orders")
                                .discountType(Coupon.DiscountType.PERCENTAGE)
                                .discountValue(new BigDecimal("10"))
                                .minOrderAmount(new BigDecimal("500"))
                                .maxDiscountAmount(new BigDecimal("500"))
                                .usageLimit(1000)
                                .expiresAt(LocalDateTime.now().plusMonths(6))
                                .build());

                couponRepository.save(Coupon.builder()
                                .code("SAVE200")
                                .description("Flat ₹200 off on orders above ₹2000")
                                .discountType(Coupon.DiscountType.FLAT_AMOUNT)
                                .discountValue(new BigDecimal("200"))
                                .minOrderAmount(new BigDecimal("2000"))
                                .usageLimit(500)
                                .expiresAt(LocalDateTime.now().plusMonths(3))
                                .build());

                couponRepository.save(Coupon.builder()
                                .code("WELCOME25")
                                .description("25% off for new users - max ₹1000 discount")
                                .discountType(Coupon.DiscountType.PERCENTAGE)
                                .discountValue(new BigDecimal("25"))
                                .minOrderAmount(new BigDecimal("1000"))
                                .maxDiscountAmount(new BigDecimal("1000"))
                                .usageLimit(200)
                                .expiresAt(LocalDateTime.now().plusMonths(12))
                                .build());

                log.info("✅ NeuroCart seed data initialized successfully!");
                log.info("👤 Admin: admin@neurocart.com / Admin@123");
                log.info("🏪 Vendor: techzone@vendor.com / Vendor@123");
                log.info("🛒 Customer: john@customer.com / Test@123");
        }

        private void createProduct(String name, String description, BigDecimal basePrice, int stock,
                        boolean featured, BigDecimal discount, Category category, Vendor vendor,
                        String imageUrl, int demandCount) {
                Product p = Product.builder()
                                .name(name)
                                .description(description)
                                .basePrice(basePrice)
                                .currentPrice(basePrice.multiply(
                                                BigDecimal.ONE.subtract(discount.divide(BigDecimal.valueOf(100)))))
                                .stockQuantity(stock)
                                .featured(featured)
                                .discountPercentage(discount)
                                .category(category)
                                .vendor(vendor)
                                .imageUrl(imageUrl)
                                .demandCount(demandCount)
                                .averageRating(3.5 + Math.random() * 1.5)
                                .totalReviews((int) (demandCount * 0.3))
                                .build();
                productRepository.save(p);
        }
}
