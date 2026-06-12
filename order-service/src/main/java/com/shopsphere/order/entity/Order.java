package com.shopsphere.order.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String orderId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private Double totalAmount;

    @Column(nullable = false)
    private String status;

    private String shippingAddress;

    private String billingAddress;

    private String paymentMethod;

    private String trackingNumber;

    @Column(nullable = false)
    private Long createdAt = System.currentTimeMillis();

    private Long updatedAt;
}
