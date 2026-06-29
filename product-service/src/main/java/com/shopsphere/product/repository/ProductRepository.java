package com.shopsphere.product.repository;

import com.shopsphere.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, String> {
    List<Product> findByCategoryIgnoreCase(String category);

    List<Product> findByActiveTrue();

    List<Product> findByNameContainingIgnoreCase(String keyword);

}
