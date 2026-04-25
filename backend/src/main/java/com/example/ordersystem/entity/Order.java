package com.example.ordersystem.entity;

import com.example.ordersystem.enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class Order {
    private String id;
    private String parentOrderId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private List<OrderItem> items;
    private String warehouseId;
    private Long createdAt;
    private Long updatedAt;
    private Long expireAt;
}