package com.example.ordersystem.service;

import com.example.ordersystem.entity.Product;
import com.example.ordersystem.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public Product createProduct(Product product) {
        product.setEnabled(true);
        return productRepository.save(product);
    }

    public Product updateProduct(String id, Product product) {
        Optional<Product> existingOpt = productRepository.findById(id);
        if (!existingOpt.isPresent()) {
            throw new RuntimeException("商品不存在");
        }
        Product existing = existingOpt.get();
        if (product.getName() != null) {
            existing.setName(product.getName());
        }
        if (product.getCategory() != null) {
            existing.setCategory(product.getCategory());
        }
        if (product.getPrice() != null) {
            existing.setPrice(product.getPrice());
        }
        if (product.getEnabled() != null) {
            existing.setEnabled(product.getEnabled());
        }
        return productRepository.save(existing);
    }

    public Optional<Product> getProductById(String id) {
        return productRepository.findById(id);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getAllEnabledProducts() {
        return productRepository.findAllEnabled();
    }

    public void deleteProduct(String id) {
        productRepository.deleteById(id);
    }
}