package com.neurocart.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_views")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 1;

    @CreationTimestamp
    @Column(name = "last_viewed_at")
    private LocalDateTime lastViewedAt;
}
