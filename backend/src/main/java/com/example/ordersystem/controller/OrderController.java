package com.example.ordersystem.controller;

import com.example.ordersystem.common.Result;
import com.example.ordersystem.dto.CreateOrderRequest;
import com.example.ordersystem.dto.OrderDetailVO;
import com.example.ordersystem.dto.OrderListVO;
import com.example.ordersystem.entity.Order;
import com.example.ordersystem.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public Result<List<Order>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        try {
            List<Order> orders = orderService.createOrder(request);
            return Result.success(orders);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/pay")
    public Result<Void> payOrder(@PathVariable String id) {
        try {
            orderService.payOrder(id);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancelOrder(@PathVariable String id) {
        try {
            orderService.cancelOrder(id);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<OrderDetailVO> getOrderById(@PathVariable String id) {
        return orderService.getOrderById(id)
                .map(order -> Result.success(orderService.toOrderDetailVO(order)))
                .orElse(Result.error("订单不存在"));
    }

    @GetMapping
    public Result<List<OrderListVO>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        List<OrderListVO> orderVOs = orders.stream()
                .map(orderService::toOrderListVO)
                .collect(Collectors.toList());
        return Result.success(orderVOs);
    }

    @GetMapping("/parent/{parentOrderId}")
    public Result<List<OrderDetailVO>> getChildOrders(@PathVariable String parentOrderId) {
        List<Order> childOrders = orderService.getChildOrders(parentOrderId);
        List<OrderDetailVO> orderVOs = childOrders.stream()
                .map(orderService::toOrderDetailVO)
                .collect(Collectors.toList());
        return Result.success(orderVOs);
    }
}