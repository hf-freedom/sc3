package com.example.ordersystem.repository;

import com.example.ordersystem.entity.Product;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class ProductRepository {
    private final Map<String, Product> productMap = new ConcurrentHashMap<>();

    public Product save(Product product) {
        if (product.getId() == null) {
            product.setId(UUID.randomUUID().toString());
        }
        if (product.getCreatedAt() == null) {
            product.setCreatedAt(System.currentTimeMillis());
        }
        product.setUpdatedAt(System.currentTimeMillis());
        productMap.put(product.getId(), product);
        return product;
    }

    public Optional<Product> findById(String id) {
        return Optional.ofNullable(productMap.get(id));
    }

    public List<Product> findAll() {
        return new ArrayList<>(productMap.values());
    }

    public List<Product> findAllEnabled() {
        return productMap.values().stream()
                .filter(Product::getEnabled)
                .collect(Collectors.toList());
    }

    public void deleteById(String id) {
        productMap.remove(id);
    }
}