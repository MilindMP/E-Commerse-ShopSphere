package com.shopsphere.product.service;

import java.util.List;

import com.shopsphere.common.dto.ProductDTO;
import com.shopsphere.product.entity.Product;

public interface ProductService {
    Product create(ProductDTO request);

    Product update(String id, ProductDTO request);

    Product getById(String id);

    List<Product> getAll();

    List<Product> getByCategory(String category);

    List<Product> search(String keyword);

    void delete(String id);
}
