package com.example.ordersystem.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderListVO {
    private String id;
    private String parentOrderId;
    private String status;
    private String statusDescription;
    private BigDecimal totalAmount;
    private Integer itemCount;
    private Boolean isParentOrder;
    private Long createdAt;
    private Long expireAt;
}