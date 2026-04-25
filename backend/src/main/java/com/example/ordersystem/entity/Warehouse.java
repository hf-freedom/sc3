package com.example.ordersystem.entity;

import lombok.Data;

@Data
public class Warehouse {
    private String id;
    private String name;
    private Integer priority;
    private Boolean enabled;
    private Long createdAt;
    private Long updatedAt;
}