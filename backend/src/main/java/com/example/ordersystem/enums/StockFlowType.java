package com.example.ordersystem.enums;

public enum StockFlowType {
    LOCK("锁定"),
    RELEASE("释放"),
    DEDUCT("扣减"),
    ROLLBACK("回滚");

    private final String description;

    StockFlowType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}