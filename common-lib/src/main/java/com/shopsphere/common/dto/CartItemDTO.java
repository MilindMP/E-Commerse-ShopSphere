package com.shopsphere.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO implements Serializable {
    private String productId;
    private Integer quantity;
    private java.math.BigDecimal price;
}
