package com.example.ordersystem.dto;

import com.example.ordersystem.entity.Order;
import com.example.ordersystem.entity.OrderItem;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderDetailVO {
    private String id;
    private String parentOrderId;
    private String status;
    private String statusDescription;
    private BigDecimal totalAmount;
    private List<OrderItemVO> items;
    private String warehouseId;
    private String warehouseName;
    private Long createdAt;
    private Long updatedAt;
    private Long expireAt;

    @Data
    public static class OrderItemVO {
        private String id;
        private String productId;
        private String productName;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal subtotal;
        private String warehouseId;
        private String warehouseName;
        private Integer deliveredQuantity;
    }
}