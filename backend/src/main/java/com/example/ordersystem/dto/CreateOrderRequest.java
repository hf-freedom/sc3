package com.example.ordersystem.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class CreateOrderRequest {
    @NotEmpty(message = "订单商品不能为空")
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {
        @NotNull(message = "商品ID不能为空")
        private String productId;
        @NotNull(message = "购买数量不能为空")
        private Integer quantity;
    }
}