package com.example.ordersystem.entity;

import lombok.Data;

@Data
public class WarehouseStock {
    private String id;
    private String productId;
    private String warehouseId;
    private Integer availableStock;
    private Integer lockedStock;
    private Long createdAt;
    private Long updatedAt;
}