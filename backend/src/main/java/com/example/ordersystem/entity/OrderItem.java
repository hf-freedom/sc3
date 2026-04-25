package com.example.ordersystem.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItem {
    private String id;
    private String orderId;
    private String productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
    private String warehouseId;
    private Integer deliveredQuantity;
}