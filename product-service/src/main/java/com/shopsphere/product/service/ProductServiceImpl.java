package com.shopsphere.product.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.shopsphere.common.dto.ProductDTO;
import com.shopsphere.product.entity.Product;
import com.shopsphere.product.repository.ProductRepository;

public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductRepository productRepository;

    @Override
    public Product create(ProductDTO request) {
        return productRepository.save(new Product(
                null,
                request.getName(),
                request.getDescription(),
                request.getPrice() == null ? null : request.getPrice().doubleValue(),
                request.getCategory(),
                request.getStock(),
                request.getImageUrl(),
                0.0,
                0,
                true,
                System.currentTimeMillis(),
                null));
    }

    @Override
    public Product update(String id, ProductDTO request) {
        return productRepository.findById(id)
                .map(existingProduct -> {
                    existingProduct.setName(request.getName());
                    existingProduct.setDescription(request.getDescription());
                    existingProduct.setPrice(request.getPrice() == null ? null : request.getPrice().doubleValue());
                    existingProduct.setCategory(request.getCategory());
                    existingProduct.setStock(request.getStock());
                    existingProduct.setImageUrl(request.getImageUrl());
                    existingProduct.setActive(request.getActive());
                    existingProduct.setUpdatedAt(System.currentTimeMillis());
                    return productRepository.save(existingProduct);
                })
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    @Override
    public Product getById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    @Override
    public List<Product> getAll() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> getByCategory(String category) {
        return productRepository.findByCategoryIgnoreCase(category);
    }

    @Override
    public List<Product> search(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }

    @Override
    public void delete(String id) {
        productRepository.deleteById(id);
    }

}
