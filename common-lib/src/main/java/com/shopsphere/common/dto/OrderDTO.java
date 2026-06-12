package com.shopsphere.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO implements Serializable {
    private String orderId;
    private String userId;
    private List<OrderItemDTO> items;
    private BigDecimal totalAmount;
    private String status; // PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    private String idempotencyKey;
    private Long createdAt;
}
