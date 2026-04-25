package com.example.ordersystem.entity;

import com.example.ordersystem.enums.StockFlowType;
import lombok.Data;

@Data
public class StockFlow {
    private String id;
    private String productId;
    private String warehouseId;
    private String orderId;
    private StockFlowType type;
    private Integer quantity;
    private Integer beforeAvailable;
    private Integer afterAvailable;
    private Integer beforeLocked;
    private Integer afterLocked;
    private String remark;
    private Long createdAt;
}