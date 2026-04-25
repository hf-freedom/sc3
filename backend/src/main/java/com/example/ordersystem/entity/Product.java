package com.example.ordersystem.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Product {
    private String id;
    private String name;
    private String category;
    private BigDecimal price;
    private Boolean enabled;
    private Long createdAt;
    private Long updatedAt;
}