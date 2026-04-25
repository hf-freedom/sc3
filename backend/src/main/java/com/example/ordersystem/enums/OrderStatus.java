package com.example.ordersystem.enums;

public enum OrderStatus {
    PENDING_PAYMENT("待支付"),
    PAID("已支付"),
    CANCELLED("已取消"),
    SHIPPED("已发货"),
    PARTIALLY_SHIPPED("部分发货"),
    COMPLETED("已完成"),
    REFUNDING("退款中");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}