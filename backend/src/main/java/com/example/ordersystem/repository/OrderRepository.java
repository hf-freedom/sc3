package com.example.ordersystem.repository;

import com.example.ordersystem.entity.Order;
import com.example.ordersystem.enums.OrderStatus;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class OrderRepository {
    private final Map<String, Order> orderMap = new ConcurrentHashMap<>();

    public Order save(Order order) {
        if (order.getId() == null) {
            order.setId(UUID.randomUUID().toString());
        }
        if (order.getCreatedAt() == null) {
            order.setCreatedAt(System.currentTimeMillis());
        }
        order.setUpdatedAt(System.currentTimeMillis());
        orderMap.put(order.getId(), order);
        return order;
    }

    public List<Order> saveAll(List<Order> orders) {
        return orders.stream()
                .map(this::save)
                .collect(Collectors.toList());
    }

    public Optional<Order> findById(String id) {
        return Optional.ofNullable(orderMap.get(id));
    }

    public List<Order> findAll() {
        return orderMap.values().stream()
                .sorted(Comparator.comparingLong(Order::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<Order> findByParentOrderId(String parentOrderId) {
        return orderMap.values().stream()
                .filter(o -> parentOrderId.equals(o.getParentOrderId()))
                .collect(Collectors.toList());
    }

    public List<Order> findExpiredOrders() {
        long now = System.currentTimeMillis();
        return orderMap.values().stream()
                .filter(o -> o.getStatus() == OrderStatus.PENDING_PAYMENT)
                .filter(o -> o.getExpireAt() != null && o.getExpireAt() < now)
                .collect(Collectors.toList());
    }

    public void deleteById(String id) {
        orderMap.remove(id);
    }
}