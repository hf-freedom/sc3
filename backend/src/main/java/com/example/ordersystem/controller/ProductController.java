package com.example.ordersystem.controller;

import com.example.ordersystem.common.Result;
import com.example.ordersystem.entity.Product;
import com.example.ordersystem.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping
    public Result<Product> createProduct(@RequestBody Product product) {
        Product created = productService.createProduct(product);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    public Result<Product> updateProduct(@PathVariable String id, @RequestBody Product product) {
        try {
            Product updated = productService.updateProduct(id, product);
            return Result.success(updated);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<Product> getProductById(@PathVariable String id) {
        return productService.getProductById(id)
                .map(Result::success)
                .orElse(Result.error("商品不存在"));
    }

    @GetMapping
    public Result<List<Product>> getAllProducts(
            @RequestParam(required = false, defaultValue = "false") Boolean enabledOnly) {
        List<Product> products;
        if (enabledOnly) {
            products = productService.getAllEnabledProducts();
        } else {
            products = productService.getAllProducts();
        }
        return Result.success(products);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return Result.success();
    }
}