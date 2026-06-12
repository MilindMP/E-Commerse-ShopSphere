package com.shopsphere.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO implements Serializable {
    private String productId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer quantity;
    private String category;
    private Boolean active;
    private Long createdAt;
}
