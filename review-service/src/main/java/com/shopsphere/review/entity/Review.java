package com.shopsphere.review.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String reviewId;

    @Column(nullable = false)
    private String productId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private Integer rating;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String content;

    private Integer helpfulCount = 0;

    @Column(nullable = false)
    private Boolean approved = false;

    @Column(nullable = false)
    private Long createdAt = System.currentTimeMillis();

    private Long updatedAt;
}
